package com.AgroLink.ProyectoAngular.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.AgroLink.ProyectoAngular.model.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByCultivoId(Long cultivoId);

    List<Lote> findByCultivoIdIn(List<Long> cultivoIds);

    List<Lote> findByPublicadoTrue();

    /**
     * RF05 / RF25 — Búsqueda con filtros opcionales.
     * Todos los parámetros son opcionales: si llegan null se ignoran.
     *
     * Se usa SQL nativo en lugar de JPQL para evitar el bug de Hibernate 6
     * con PostgreSQL donde parámetros null en LOWER() se infieren como bytea
     * en vez de text, causando "function lower(bytea) does not exist".
     * Se usa CAST(col AS text) en lugar de col::text porque Spring Data
     * interpreta '::' como inicio de parámetro nombrado y corrompe la query.
     */
    @Query(value = """
        SELECT l.* FROM lote l
        JOIN cultivo c ON l.cultivo_id = c.id
        WHERE l.publicado = true
          AND l.stock_disponible > 0
          AND (:calidad    IS NULL OR l.calidad = :calidad)
          AND (:precioMin  IS NULL OR l.precio_unitario >= CAST(:precioMin AS numeric))
          AND (:precioMax  IS NULL OR l.precio_unitario <= CAST(:precioMax AS numeric))
          AND (:categoria  IS NULL OR CAST(c.categoria AS text) = :categoria)
          AND (:ubicacion  IS NULL OR LOWER(CAST(c.ubicacion AS text))      LIKE LOWER(CONCAT('%', :ubicacion, '%')))
          AND (:busqueda   IS NULL OR LOWER(CAST(c.nombre_producto AS text)) LIKE LOWER(CONCAT('%', :busqueda, '%'))
                                   OR LOWER(COALESCE(c.variedad, ''))        LIKE LOWER(CONCAT('%', :busqueda, '%')))
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

    /**
     * RF-19 — Atención de pedidos parciales.
     * Busca lotes alternativos (de cualquier agricultor) que ofrezcan el mismo
     * producto y tengan stock disponible, para completar un pedido cuando el
     * lote originalmente elegido por el comprador no alcanza a cubrirlo.
     * Se excluye el lote de origen y se ordena por mejor precio y, en empate,
     * por antigüedad de publicación (FIFO).
     */
    @Query(value = """
        SELECT l.* FROM lote l
        JOIN cultivo c ON l.cultivo_id = c.id
        WHERE l.publicado = true
          AND l.estado = 'ACTIVO'
          AND l.stock_disponible > 0
          AND l.id <> :loteExcluidoId
          AND LOWER(CAST(c.nombre_producto AS text)) = LOWER(:nombreProducto)
        ORDER BY l.precio_unitario ASC, l.fecha_publicacion ASC
    """, nativeQuery = true)
    List<Lote> buscarLotesAlternativosParaProducto(
        @Param("nombreProducto") String nombreProducto,
        @Param("loteExcluidoId") Long loteExcluidoId
    );
}