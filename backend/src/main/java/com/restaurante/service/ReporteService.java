package com.restaurante.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.restaurante.model.*;
import com.restaurante.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final InsumoRepository insumoRepository;
    private final PagoRepository pagoRepository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;
    private final CategoriaPagoRepository categoriaPagoRepository;

    public ReporteService(PedidoRepository pedidoRepository, ComensalRepository comensalRepository,
                          MesaRepository mesaRepository, DetallePedidoRepository detallePedidoRepository,
                          InsumoRepository insumoRepository, PagoRepository pagoRepository,
                          CategoriaInsumoRepository categoriaInsumoRepository,
                          CategoriaPagoRepository categoriaPagoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.mesaRepository = mesaRepository;
        this.insumoRepository = insumoRepository;
        this.pagoRepository = pagoRepository;
        this.categoriaInsumoRepository = categoriaInsumoRepository;
        this.categoriaPagoRepository = categoriaPagoRepository;
    }

    public byte[] generarTicketMesa(Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + mesaId));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A5);
        PdfWriter.getInstance(doc, out);
        doc.open();

        doc.add(new Paragraph("TICKET", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        addField(doc, "Mesa", "Mesa " + mesa.getNumero());
        doc.add(new Paragraph("----------------------------------------", FontFactory.getFont(FontFactory.HELVETICA, 7)));

        List<Pedido> pedidos = pedidoRepository.findByMesaIdWithDetallesAndEstadoAbierto(mesaId);
        double granTotal = 0;

        for (Pedido p : pedidos) {
            String comensalNombre = p.getComensal() != null ? p.getComensal().getNombre() : "—";
            doc.add(new Paragraph("Com: " + comensalNombre, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
            doc.add(new Paragraph("----------------------------------------", FontFactory.getFont(FontFactory.HELVETICA, 7)));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{3, 1, 2, 2});

            addCell(table, "Producto", true);
            addCell(table, "Cant", true);
            addCell(table, "P.U.", true);
            addCell(table, "Subtotal", true);

            double subTotal = 0;
            for (DetallePedido d : p.getDetalles()) {
                addCell(table, d.getProducto().getNombre(), false);
                addCell(table, String.valueOf(d.getCantidad()), false);
                addCell(table, String.format("$%.2f", d.getPrecioUnitario()), false);
                addCell(table, String.format("$%.2f", d.getSubtotal()), false);
                subTotal += d.getSubtotal();
            }
            doc.add(table);
            doc.add(new Paragraph("Subtotal: $" + String.format("%.2f", subTotal), FontFactory.getFont(FontFactory.HELVETICA, 9)));
            granTotal += subTotal;
        }

        doc.add(new Paragraph("----------------------------------------", FontFactory.getFont(FontFactory.HELVETICA, 7)));
        doc.add(new Paragraph("TOTAL: $" + String.format("%.2f", granTotal), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        doc.add(new Paragraph("Fecha: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")), FontFactory.getFont(FontFactory.HELVETICA, 8)));

        doc.close();
        return out.toByteArray();
    }

    @Transactional
    public byte[] generarComandaMesa(Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + mesaId));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A5);
        PdfWriter.getInstance(doc, out);
        doc.open();

        doc.add(new Paragraph("COMANDA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
        doc.add(new Paragraph("Mesa: " + mesa.getNumero(), FontFactory.getFont(FontFactory.HELVETICA, 9)));
        doc.add(new Paragraph("----------------------------------------", FontFactory.getFont(FontFactory.HELVETICA, 7)));

        List<Pedido> pedidos = pedidoRepository.findByMesaIdWithDetallesAndEstadoAbierto(mesaId);

        for (Pedido p : pedidos) {
            String comensalNombre = p.getComensal() != null ? p.getComensal().getNombre() : "—";
            doc.add(new Paragraph("Com: " + comensalNombre, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            for (DetallePedido d : p.getDetalles()) {
                doc.add(new Paragraph("  " + d.getCantidad() + "x " + d.getProducto().getNombre(),
                        FontFactory.getFont(FontFactory.HELVETICA, 9)));
            }
        }

        for (Pedido pedido : pedidos) {
            pedido.setEstado("COMIENDO");
            pedidoRepository.save(pedido);
        }

        doc.close();
        return out.toByteArray();
    }

    public byte[] generarReporteVentas(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "REPORTE DE VENTAS");
        addField(doc, "Periodo", inicio + " a " + fin);
        doc.add(Chunk.NEWLINE);

        List<Pedido> pedidos = pedidoRepository.findCerradosEntreFechas(
                inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 2, 2});

        addCell(table, "Fecha", true);
        addCell(table, "Mesa", true);
        addCell(table, "Productos", true);
        addCell(table, "Total", true);

        double granTotal = 0;
        for (Pedido p : pedidos) {
            addCell(table, p.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), false);
            addCell(table, "Mesa " + p.getMesa().getNumero(), false);
            addCell(table, String.valueOf(p.getDetalles().size()), false);
            addCell(table, String.format("$%.2f", p.getTotal()), false);
            granTotal += p.getTotal();
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("TOTAL VENTAS: $" + String.format("%.2f", granTotal),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

        doc.close();
        return out.toByteArray();
    }

    public byte[] generarReporteVentasResumen(String fechaInicio, String fechaFin) {
        LocalDate inicio = LocalDate.parse(fechaInicio);
        LocalDate fin = LocalDate.parse(fechaFin);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "REPORTE DE VENTAS (RESUMEN)");
        addField(doc, "Periodo", inicio + " a " + fin);
        doc.add(Chunk.NEWLINE);

        List<Pedido> pedidos = pedidoRepository.findCerradosEntreFechas(
                inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));

        Map<LocalDate, Double> totalesPorDia = new LinkedHashMap<>();
        for (Pedido p : pedidos) {
            LocalDate dia = p.getFecha().toLocalDate();
            totalesPorDia.merge(dia, p.getTotal(), Double::sum);
        }

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2});

        addCell(table, "Fecha", true);
        addCell(table, "Total", true);

        double granTotal = 0;
        for (Map.Entry<LocalDate, Double> entry : totalesPorDia.entrySet()) {
            addCell(table, entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), false);
            addCell(table, String.format("$%.2f", entry.getValue()), false);
            granTotal += entry.getValue();
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("TOTAL VENTAS: $" + String.format("%.2f", granTotal),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

        doc.close();
        return out.toByteArray();
    }

    public byte[] generarReporteInsumos(Long categoriaId, Integer mes, Integer anio) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "REPORTE DE INSUMOS");

        if (mes != null && anio != null) {
            String[] meses = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
            addField(doc, "Periodo", meses[mes - 1] + " " + anio);
        }

        LocalDate inicio = null, fin = null;
        if (mes != null && anio != null) {
            inicio = LocalDate.of(anio, mes, 1);
            fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        }

        List<Insumo> insumos;
        if (categoriaId != null) {
            CategoriaInsumo cat = categoriaInsumoRepository.findById(categoriaId)
                    .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
            addField(doc, "Categoria", cat.getNombre());
            if (inicio != null) {
                insumos = insumoRepository.findByCategoriaInsumoIdAndFechaIngresoBetweenOrderByNombreAsc(
                        categoriaId, inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));
            } else {
                insumos = insumoRepository.findByCategoriaInsumoIdOrderByNombreAsc(categoriaId);
            }
        } else {
            addField(doc, "Categoria", "TODOS");
            if (inicio != null) {
                insumos = insumoRepository.findByFechaIngresoBetweenOrderByNombreAsc(
                        inicio.atStartOfDay(), fin.atTime(LocalTime.MAX));
            } else {
                insumos = insumoRepository.findAll();
            }
        }

        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 2, 2, 2, 2});

        addCell(table, "Nombre", true);
        addCell(table, "Fecha Ingreso", true);
        addCell(table, "Cantidad", true);
        addCell(table, "Unidad", true);
        addCell(table, "Precio Unit.", true);
        addCell(table, "Total", true);

        double granTotal = 0;
        for (Insumo i : insumos) {
            addCell(table, i.getNombre(), false);
            addCell(table, i.getFechaIngreso().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), false);
            addCell(table, String.valueOf(i.getCantidad()), false);
            addCell(table, i.getUnidad(), false);
            addCell(table, String.format("$%.2f", i.getPrecioUnitario()), false);
            double total = i.getCantidad() * i.getPrecioUnitario();
            addCell(table, String.format("$%.2f", total), false);
            granTotal += total;
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("TOTAL INVERSIÓN: $" + String.format("%.2f", granTotal),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

        doc.close();
        return out.toByteArray();
    }

    public byte[] generarReportePagos(Long categoriaId, Integer mes, Integer anio) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "REPORTE DE PAGOS");

        if (mes != null && anio != null) {
            String[] meses = {"ENERO","FEBRERO","MARZO","ABRIL","MAYO","JUNIO","JULIO","AGOSTO","SEPTIEMBRE","OCTUBRE","NOVIEMBRE","DICIEMBRE"};
            addField(doc, "Periodo", meses[mes - 1] + " " + anio);
        }

        LocalDateTime inicio = null, fin = null;
        if (mes != null && anio != null) {
            YearMonth ym = YearMonth.of(anio, mes);
            inicio = ym.atDay(1).atStartOfDay();
            fin = ym.atEndOfMonth().atTime(LocalTime.MAX);
        }

        List<Pago> pagos;
        if (categoriaId != null) {
            CategoriaPago cat = categoriaPagoRepository.findById(categoriaId)
                    .orElseThrow(() -> new RuntimeException("Categoria no encontrada"));
            addField(doc, "Categoria", cat.getNombre());
            if (inicio != null) {
                pagos = pagoRepository.findByCategoriaPagoIdAndFechaBetweenOrderByFechaDesc(categoriaId, inicio, fin);
            } else {
                pagos = pagoRepository.findByCategoriaPagoIdOrderByFechaDesc(categoriaId);
            }
        } else {
            addField(doc, "Categoria", "TODOS");
            if (inicio != null) {
                pagos = pagoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
            } else {
                pagos = pagoRepository.findAll();
            }
        }

        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 2, 2});

        addCell(table, "Concepto", true);
        addCell(table, "Fecha", true);
        addCell(table, "Categoria", true);
        addCell(table, "Monto", true);

        double granTotal = 0;
        for (Pago p : pagos) {
            addCell(table, p.getConcepto(), false);
            addCell(table, p.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), false);
            addCell(table, p.getCategoriaPago().getNombre(), false);
            addCell(table, String.format("$%.2f", p.getMonto()), false);
            granTotal += p.getMonto();
        }

        doc.add(table);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("TOTAL PAGOS: $" + String.format("%.2f", granTotal),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));

        doc.close();
        return out.toByteArray();
    }

    private void addHeader(Document doc, String title) throws DocumentException {
        doc.add(new Paragraph("MI RESTAURANTE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        doc.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        doc.add(new Paragraph("========================================"));
        doc.add(Chunk.NEWLINE);
    }

    private void addField(Document doc, String label, String value) throws DocumentException {
        doc.add(new Paragraph(label + ": " + value, FontFactory.getFont(FontFactory.HELVETICA, 9)));
    }

    private void addCell(PdfPTable table, String text, boolean bold) {
        PdfPCell cell = new PdfPCell(new Paragraph(text,
                bold ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8) : FontFactory.getFont(FontFactory.HELVETICA, 8)));
        cell.setPadding(2);
        table.addCell(cell);
    }
}
