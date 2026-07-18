package com.AgroLink.ProyectoAngular.service;

import com.AgroLink.ProyectoAngular.dto.reporte.ReporteVentasResponse;
import com.AgroLink.ProyectoAngular.dto.reporte.VentaPorAgricultorDTO;
import com.AgroLink.ProyectoAngular.dto.reporte.VentaPorProductoDTO;
import com.AgroLink.ProyectoAngular.model.HistorialPrecio;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;

/**
 * RF-26 — Exportación de reportes comerciales (RF-10) a PDF y Excel.
 */
@Service
public class ReporteExportService {

    private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ───────────────────────────── PDF ─────────────────────────────

    public byte[] generarPdf(ReporteVentasResponse r) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(30, 90, 40));
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font seccionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(30, 90, 40));
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);

            doc.add(new Paragraph("AgroLink — Reporte Comercial", tituloFont));
            String periodo = "Periodo: " + (r.getDesde() != null ? r.getDesde() : "inicio")
                    + " al " + (r.getHasta() != null ? r.getHasta() : "hoy") + "   |   Alcance: " + r.getAlcance();
            doc.add(new Paragraph(periodo, subtituloFont));
            doc.add(new Paragraph(" "));

            // Resumen
            doc.add(new Paragraph("Resumen general", seccionFont));
            PdfPTable resumen = new PdfPTable(2);
            resumen.setWidthPercentage(60);
            resumen.setSpacingBefore(6);
            resumen.setSpacingAfter(12);
            agregarFilaResumen(resumen, "Total de pedidos", String.valueOf(r.getTotalPedidos()), cellFont);
            agregarFilaResumen(resumen, "Total ingresos (S/.)",
                    r.getTotalIngresos() != null ? r.getTotalIngresos().toPlainString() : "0.00", cellFont);
            if (r.getPedidosPorEstado() != null) {
                r.getPedidosPorEstado().forEach((estado, cantidad) ->
                        agregarFilaResumen(resumen, "Pedidos " + estado, String.valueOf(cantidad), cellFont));
            }
            doc.add(resumen);

            // Ventas por producto
            if (r.getVentasPorProducto() != null && !r.getVentasPorProducto().isEmpty()) {
                doc.add(new Paragraph("Ventas por producto", seccionFont));
                PdfPTable tabla = new PdfPTable(new float[]{3, 2, 2, 2, 1.5f});
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(6);
                tabla.setSpacingAfter(12);
                for (String h : new String[]{"Producto", "Categoría", "Kg vendidos", "Monto (S/.)", "Pedidos"}) {
                    agregarHeader(tabla, h, headerFont);
                }
                for (VentaPorProductoDTO v : r.getVentasPorProducto()) {
                    agregarCelda(tabla, v.getNombreProducto(), cellFont);
                    agregarCelda(tabla, v.getCategoria(), cellFont);
                    agregarCelda(tabla, String.format("%.2f", v.getCantidadVendidaKg()), cellFont);
                    agregarCelda(tabla, v.getMontoTotal().toPlainString(), cellFont);
                    agregarCelda(tabla, String.valueOf(v.getNumeroPedidos()), cellFont);
                }
                doc.add(tabla);
            }

            // Ventas por agricultor (solo vista global)
            if (r.getVentasPorAgricultor() != null && !r.getVentasPorAgricultor().isEmpty()) {
                doc.add(new Paragraph("Ventas por agricultor", seccionFont));
                PdfPTable tabla = new PdfPTable(new float[]{3, 2, 2});
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(6);
                tabla.setSpacingAfter(12);
                for (String h : new String[]{"Agricultor", "Pedidos", "Monto (S/.)"}) {
                    agregarHeader(tabla, h, headerFont);
                }
                for (VentaPorAgricultorDTO v : r.getVentasPorAgricultor()) {
                    agregarCelda(tabla, v.getNombreAgricultor(), cellFont);
                    agregarCelda(tabla, String.valueOf(v.getNumeroPedidos()), cellFont);
                    agregarCelda(tabla, v.getMontoTotal().toPlainString(), cellFont);
                }
                doc.add(tabla);
            }

            // Cambios de precio (RF-20)
            if (r.getCambiosPrecio() != null && !r.getCambiosPrecio().isEmpty()) {
                doc.add(new Paragraph("Cambios de precio en el periodo", seccionFont));
                PdfPTable tabla = new PdfPTable(new float[]{1, 1.5f, 1.5f, 2, 2.5f});
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(6);
                for (String h : new String[]{"Lote", "Precio anterior", "Precio nuevo", "Responsable", "Fecha"}) {
                    agregarHeader(tabla, h, headerFont);
                }
                for (HistorialPrecio hp : r.getCambiosPrecio()) {
                    agregarCelda(tabla, String.valueOf(hp.getLoteId()), cellFont);
                    agregarCelda(tabla, hp.getPrecioAnterior().toPlainString(), cellFont);
                    agregarCelda(tabla, hp.getPrecioNuevo().toPlainString(), cellFont);
                    agregarCelda(tabla, hp.getUsuarioResponsableEmail(), cellFont);
                    agregarCelda(tabla, hp.getFechaCambio().format(FECHA_FMT), cellFont);
                }
                doc.add(tabla);
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando el PDF del reporte", e);
        }
    }

    private void agregarFilaResumen(PdfPTable tabla, String etiqueta, String valor, Font font) {
        PdfPCell c1 = new PdfPCell(new Paragraph(etiqueta, font));
        c1.setBorder(0);
        c1.setPadding(4);
        PdfPCell c2 = new PdfPCell(new Paragraph(valor, font));
        c2.setBorder(0);
        c2.setPadding(4);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.addCell(c1);
        tabla.addCell(c2);
    }

    private void agregarHeader(PdfPTable tabla, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(texto, font));
        cell.setBackgroundColor(new Color(46, 125, 50));
        cell.setPadding(5);
        tabla.addCell(cell);
    }

    private void agregarCelda(PdfPTable tabla, String texto, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(texto != null ? texto : "-", font));
        cell.setPadding(4);
        tabla.addCell(cell);
    }

    // ───────────────────────────── Excel ─────────────────────────────

    public byte[] generarExcel(ReporteVentasResponse r) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREEN.getIndex());
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // Hoja 1: Resumen
            Sheet resumen = workbook.createSheet("Resumen");
            int fila = 0;
            fila = escribirFila(resumen, fila, headerStyle, "Periodo",
                    (r.getDesde() != null ? r.getDesde().toString() : "inicio") + " a "
                            + (r.getHasta() != null ? r.getHasta().toString() : "hoy"));
            fila = escribirFila(resumen, fila, headerStyle, "Alcance", r.getAlcance());
            fila = escribirFila(resumen, fila, headerStyle, "Total pedidos", String.valueOf(r.getTotalPedidos()));
            fila = escribirFila(resumen, fila, headerStyle, "Total ingresos (S/.)",
                    r.getTotalIngresos() != null ? r.getTotalIngresos().toPlainString() : "0.00");
            if (r.getPedidosPorEstado() != null) {
                for (var e : r.getPedidosPorEstado().entrySet()) {
                    fila = escribirFila(resumen, fila, headerStyle, "Pedidos " + e.getKey(), String.valueOf(e.getValue()));
                }
            }
            for (int c = 0; c < 2; c++) resumen.autoSizeColumn(c);

            // Hoja 2: Ventas por producto
            Sheet hojaProducto = workbook.createSheet("Ventas por producto");
            Row hp = hojaProducto.createRow(0);
            String[] headersProducto = {"Producto", "Categoría", "Kg vendidos", "Monto (S/.)", "N° Pedidos"};
            for (int i = 0; i < headersProducto.length; i++) {
                crearCeldaHeader(hp, i, headersProducto[i], headerStyle);
            }
            if (r.getVentasPorProducto() != null) {
                int rowIdx = 1;
                for (VentaPorProductoDTO v : r.getVentasPorProducto()) {
                    Row row = hojaProducto.createRow(rowIdx++);
                    row.createCell(0).setCellValue(v.getNombreProducto());
                    row.createCell(1).setCellValue(v.getCategoria());
                    row.createCell(2).setCellValue(v.getCantidadVendidaKg());
                    row.createCell(3).setCellValue(v.getMontoTotal().doubleValue());
                    row.createCell(4).setCellValue(v.getNumeroPedidos());
                }
            }
            for (int c = 0; c < headersProducto.length; c++) hojaProducto.autoSizeColumn(c);

            // Hoja 3: Ventas por agricultor (si aplica)
            if (r.getVentasPorAgricultor() != null && !r.getVentasPorAgricultor().isEmpty()) {
                Sheet hojaAgricultor = workbook.createSheet("Ventas por agricultor");
                Row ha = hojaAgricultor.createRow(0);
                String[] headersAgricultor = {"Agricultor", "N° Pedidos", "Monto (S/.)"};
                for (int i = 0; i < headersAgricultor.length; i++) {
                    crearCeldaHeader(ha, i, headersAgricultor[i], headerStyle);
                }
                int rowIdx = 1;
                for (VentaPorAgricultorDTO v : r.getVentasPorAgricultor()) {
                    Row row = hojaAgricultor.createRow(rowIdx++);
                    row.createCell(0).setCellValue(v.getNombreAgricultor());
                    row.createCell(1).setCellValue(v.getNumeroPedidos());
                    row.createCell(2).setCellValue(v.getMontoTotal().doubleValue());
                }
                for (int c = 0; c < headersAgricultor.length; c++) hojaAgricultor.autoSizeColumn(c);
            }

            // Hoja 4: Cambios de precio (RF-20)
            if (r.getCambiosPrecio() != null && !r.getCambiosPrecio().isEmpty()) {
                Sheet hojaPrecio = workbook.createSheet("Cambios de precio");
                Row hcp = hojaPrecio.createRow(0);
                String[] headersPrecio = {"Lote", "Precio anterior", "Precio nuevo", "Responsable", "Motivo", "Fecha"};
                for (int i = 0; i < headersPrecio.length; i++) {
                    crearCeldaHeader(hcp, i, headersPrecio[i], headerStyle);
                }
                int rowIdx = 1;
                for (HistorialPrecio h : r.getCambiosPrecio()) {
                    Row row = hojaPrecio.createRow(rowIdx++);
                    row.createCell(0).setCellValue(h.getLoteId());
                    row.createCell(1).setCellValue(h.getPrecioAnterior().doubleValue());
                    row.createCell(2).setCellValue(h.getPrecioNuevo().doubleValue());
                    row.createCell(3).setCellValue(h.getUsuarioResponsableEmail());
                    row.createCell(4).setCellValue(h.getMotivo() != null ? h.getMotivo() : "");
                    row.createCell(5).setCellValue(h.getFechaCambio().format(FECHA_FMT));
                }
                for (int c = 0; c < headersPrecio.length; c++) hojaPrecio.autoSizeColumn(c);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Error generando el Excel del reporte", e);
        }
    }

    private int escribirFila(Sheet sheet, int filaIdx, CellStyle headerStyle, String etiqueta, String valor) {
        Row row = sheet.createRow(filaIdx);
        Cell c0 = row.createCell(0);
        c0.setCellValue(etiqueta);
        c0.setCellStyle(headerStyle);
        row.createCell(1).setCellValue(valor);
        return filaIdx + 1;
    }

    private void crearCeldaHeader(Row row, int col, String texto, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(texto);
        cell.setCellStyle(style);
    }
}
