package com.AgroLink.ProyectoAngular.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.AgroLink.ProyectoAngular.model.Lote;
import com.AgroLink.ProyectoAngular.model.enums.CalidadLoteEnum;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByCultivoId(Long cultivoId);

    List<Lote> findByPublicadoTrue();

    /**
     * RF05 / RF25 — Búsqueda con filtros opcionales.
     * Todos los parámetros son opcionales: si llegan null se ignoran.
     * El JOIN con Cultivo permite filtrar por categoría, nombre de producto
     * y ubicación del agricultor sin duplicar datos en Lote.
     */
    @Query("""
        SELECT l FROM Lote l
        JOIN Cultivo c ON l.cultivoId = c.id
        WHERE l.publicado = true
          AND l.stockDisponible > 0
          AND (:calidad     IS NULL OR l.calidad     = :calidad)
          AND (:precioMin   IS NULL OR l.precioUnitario >= :precioMin)
          AND (:precioMax   IS NULL OR l.precioUnitario <= :precioMax)
          AND (:categoria   IS NULL OR CAST(c.categoria AS string) = :categoria)
          AND (:ubicacion   IS NULL OR LOWER(c.ubicacion)      LIKE LOWER(CONCAT('%', :ubicacion, '%')))
          AND (:busqueda    IS NULL OR LOWER(c.nombreProducto) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                                    OR LOWER(c.variedad)       LIKE LOWER(CONCAT('%', :busqueda, '%')))
          AND (:fechaDesde  IS NULL OR l.fechaEntregaEstimada >= :fechaDesde)
          AND (:fechaHasta  IS NULL OR l.fechaEntregaEstimada <= :fechaHasta)
        ORDER BY l.fechaPublicacion DESC
    """)
    List<Lote> buscarConFiltros(
        @Param("calidad")    CalidadLoteEnum calidad,
        @Param("precioMin")  BigDecimal precioMin,
        @Param("precioMax")  BigDecimal precioMax,
        @Param("categoria")  String categoria,
        @Param("ubicacion")  String ubicacion,
        @Param("busqueda")   String busqueda,
        @Param("fechaDesde") LocalDate fechaDesde,
        @Param("fechaHasta") LocalDate fechaHasta
    );
}
