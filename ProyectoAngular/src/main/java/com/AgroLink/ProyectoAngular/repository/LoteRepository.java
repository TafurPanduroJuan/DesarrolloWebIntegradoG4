package com.AgroLink.ProyectoAngular.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.AgroLink.ProyectoAngular.model.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByCultivoId(Long cultivoId);

    List<Lote> findByPublicadoTrue();

    /**
     * RF05 / RF25 — Búsqueda con filtros opcionales.
     * Todos los parámetros son opcionales: si llegan null se ignoran.
     *
     * Se usa SQL nativo en lugar de JPQL para evitar el bug de Hibernate 6
     * con PostgreSQL donde parámetros null en LOWER() se infieren como bytea
     * en vez de text, causando "function lower(bytea) does not exist".
     * El cast explícito ::text resuelve el problema de tipos en PostgreSQL.
     */
    @Query(value = """
        SELECT l.* FROM lote l
        JOIN cultivo c ON l.cultivo_id = c.id
        WHERE l.publicado = true
          AND l.stock_disponible > 0
          AND (:calidad    IS NULL OR l.calidad = :calidad)
          AND (:precioMin  IS NULL OR l.precio_unitario >= CAST(:precioMin AS numeric))
          AND (:precioMax  IS NULL OR l.precio_unitario <= CAST(:precioMax AS numeric))
          AND (:categoria  IS NULL OR c.categoria::text = :categoria)
          AND (:ubicacion  IS NULL OR LOWER(c.ubicacion::text)       LIKE LOWER(CONCAT('%', :ubicacion, '%')))
          AND (:busqueda   IS NULL OR LOWER(c.nombre_producto::text)  LIKE LOWER(CONCAT('%', :busqueda, '%'))
                                   OR LOWER(COALESCE(c.variedad, '')) LIKE LOWER(CONCAT('%', :busqueda, '%')))
          AND (:fechaDesde IS NULL OR l.fecha_entrega_estimada >= CAST(:fechaDesde AS date))
          AND (:fechaHasta IS NULL OR l.fecha_entrega_estimada <= CAST(:fechaHasta AS date))
        ORDER BY l.fecha_publicacion DESC
    """, nativeQuery = true)
    List<Lote> buscarConFiltros(
        @Param("calidad")    String calidad,
        @Param("precioMin")  BigDecimal precioMin,
        @Param("precioMax")  BigDecimal precioMax,
        @Param("categoria")  String categoria,
        @Param("ubicacion")  String ubicacion,
        @Param("busqueda")   String busqueda,
        @Param("fechaDesde") String fechaDesde,
        @Param("fechaHasta") String fechaHasta
    );
}
