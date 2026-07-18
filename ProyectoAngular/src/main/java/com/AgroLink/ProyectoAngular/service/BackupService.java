package com.AgroLink.ProyectoAngular.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RNF-09 — Backup automático de PostgreSQL.
 * Cierre natural del bloque de infraestructura (auditoría → respaldo): usa
 * pg_dump (formato custom -Fc, ya comprimido y restaurable con pg_restore)
 * en un job programado diario, con política de retención y registro en
 * la bitácora de auditoría (RF-27).
 *
 * NOTA DE DESPLIEGUE: requiere el binario "pg_dump" disponible en el
 * contenedor (agregado en el Dockerfile con `apk add postgresql-client`).
 * El directorio de respaldo es efímero en el plan gratuito de Render: si
 * se necesita retención real entre despliegues, subir los archivos a un
 * almacenamiento externo (S3, Cloudinary, un Persistent Disk de Render, etc.)
 * es la extensión natural de este servicio.
 */
@Service
public class BackupService {

    private static final DateTimeFormatter NOMBRE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^agrolink_backup_[0-9_]+\\.dump$");

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${DB_USER:postgres}")
    private String dbUser;

    @Value("${DB_PASSWORD:postgres}")
    private String dbPassword;

    @Value("${backup.dir:backups}")
    private String backupDir;

    @Value("${backup.retention:7}")
    private int retencion;

    // Render Postgres exige SSL en conexiones; ajustable si se corre contra un Postgres local sin SSL.
    @Value("${backup.sslmode:require}")
    private String sslMode;

    private final AuditoriaService auditoriaService;

    public BackupService(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    /** Job automático: todos los días a las 3:00 am (hora del servidor). */
    @Scheduled(cron = "0 0 3 * * *")
    public void backupProgramado() {
        ejecutarBackup("SCHEDULED");
    }

    public record ResultadoBackup(boolean exitoso, String archivo, String mensaje) {
    }

    public ResultadoBackup ejecutarBackup(String origen) {
        try {
            Path dir = Paths.get(backupDir);
            Files.createDirectories(dir);

            DatosConexion conexion = parsearUrl(datasourceUrl);
            String nombreArchivo = "agrolink_backup_" + LocalDateTime.now().format(NOMBRE_FMT) + ".dump";
            Path destino = dir.resolve(nombreArchivo);

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", conexion.host(),
                    "-p", conexion.puerto(),
                    "-U", dbUser,
                    "-d", conexion.baseDatos(),
                    "-Fc",
                    "-f", destino.toString()
            );
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.environment().put("PGSSLMODE", sslMode);
            pb.redirectErrorStream(true);

            Process proceso = pb.start();
            String salida = new String(proceso.getInputStream().readAllBytes());
            boolean termino = proceso.waitFor(5, TimeUnit.MINUTES);

            if (!termino) {
                proceso.destroyForcibly();
                auditoriaService.registrarAuditoria("BACKUP_DB",
                        "[" + origen + "] Backup cancelado por timeout (>5 min)");
                return new ResultadoBackup(false, null, "El backup excedió el tiempo máximo de espera");
            }

            if (proceso.exitValue() != 0) {
                auditoriaService.registrarAuditoria("BACKUP_DB",
                        "[" + origen + "] Backup fallido: " + resumir(salida));
                return new ResultadoBackup(false, null, "pg_dump finalizó con error: " + resumir(salida));
            }

            aplicarRetencion(dir);

            auditoriaService.registrarAuditoria("BACKUP_DB",
                    "[" + origen + "] Backup generado correctamente: " + nombreArchivo);
            return new ResultadoBackup(true, nombreArchivo, "Backup generado correctamente");

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            auditoriaService.registrarAuditoria("BACKUP_DB",
                    "[" + origen + "] Backup fallido: " + e.getMessage());
            return new ResultadoBackup(false, null, "Error ejecutando pg_dump: " + e.getMessage());
        }
    }

    /** Conserva solo los últimos N backups (por defecto 7); borra el resto. */
    private void aplicarRetencion(Path dir) throws IOException {
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> archivos = stream
                    .filter(p -> p.getFileName().toString().endsWith(".dump"))
                    .sorted(Comparator.comparing(this::fechaModificacion).reversed())
                    .collect(Collectors.toList());
            for (int i = retencion; i < archivos.size(); i++) {
                Files.deleteIfExists(archivos.get(i));
            }
        }
    }

    private LocalDateTime fechaModificacion(Path p) {
        try {
            return LocalDateTime.ofInstant(Files.getLastModifiedTime(p).toInstant(),
                    java.time.ZoneId.systemDefault());
        } catch (IOException e) {
            return LocalDateTime.MIN;
        }
    }

    public record BackupInfo(String nombre, long tamanioBytes, String fecha) {
    }

    public List<BackupInfo> listarBackups() {
        Path dir = Paths.get(backupDir);
        if (!Files.exists(dir)) return List.of();
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".dump"))
                    .sorted(Comparator.comparing(this::fechaModificacion).reversed())
                    .map(p -> {
                        try {
                            return new BackupInfo(p.getFileName().toString(), Files.size(p),
                                    fechaModificacion(p).toString());
                        } catch (IOException e) {
                            return new BackupInfo(p.getFileName().toString(), 0L, "");
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return List.of();
        }
    }

    /** Devuelve la ruta del backup solicitado, validando que el nombre no permita path traversal. */
    public Path resolverArchivoParaDescarga(String nombreArchivo) {
        if (nombreArchivo == null || !NOMBRE_VALIDO.matcher(nombreArchivo).matches()) {
            return null;
        }
        Path archivo = Paths.get(backupDir).resolve(nombreArchivo).normalize();
        if (!archivo.startsWith(Paths.get(backupDir).normalize()) || !Files.exists(archivo)) {
            return null;
        }
        return archivo;
    }

    private String resumir(String texto) {
        if (texto == null) return "";
        return texto.length() > 400 ? texto.substring(0, 400) + "..." : texto;
    }

    private record DatosConexion(String host, String puerto, String baseDatos) {
    }

    /**
     * Convierte una URL JDBC (jdbc:postgresql://host:puerto/basedatos?params)
     * en sus componentes para invocar pg_dump.
     */
    private DatosConexion parsearUrl(String jdbcUrl) {
        try {
            String limpio = jdbcUrl.replaceFirst("^jdbc:", "");
            URI uri = new URI(limpio);
            String host = uri.getHost() != null ? uri.getHost() : "localhost";
            String puerto = uri.getPort() != -1 ? String.valueOf(uri.getPort()) : "5432";
            String path = uri.getPath() != null ? uri.getPath() : "/postgres";
            String baseDatos = path.startsWith("/") ? path.substring(1) : path;
            return new DatosConexion(host, puerto, baseDatos);
        } catch (URISyntaxException e) {
            // Fallback con expresion regular por si la URL trae parametros no estandar
            Matcher m = Pattern.compile("//([^:/]+)(?::(\\d+))?/([^?]+)").matcher(jdbcUrl);
            if (m.find()) {
                return new DatosConexion(m.group(1), m.group(2) != null ? m.group(2) : "5432", m.group(3));
            }
            throw new IllegalStateException("No se pudo interpretar spring.datasource.url para el backup", e);
        }
    }
}
