package com.restaurante.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.restaurante.model.*;
import com.restaurante.repository.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteService {

    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final InsumoRepository insumoRepository;
    private final PagoRepository pagoRepository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;
    private final CategoriaPagoRepository categoriaPagoRepository;
    private final CategoriaRepository categoriaRepository;

    public ReporteService(PedidoRepository pedidoRepository, ComensalRepository comensalRepository,
                          MesaRepository mesaRepository, DetallePedidoRepository detallePedidoRepository,
                          InsumoRepository insumoRepository, PagoRepository pagoRepository,
                          CategoriaInsumoRepository categoriaInsumoRepository,
                          CategoriaPagoRepository categoriaPagoRepository,
                          CategoriaRepository categoriaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.mesaRepository = mesaRepository;
        this.insumoRepository = insumoRepository;
        this.pagoRepository = pagoRepository;
        this.categoriaInsumoRepository = categoriaInsumoRepository;
        this.categoriaPagoRepository = categoriaPagoRepository;
        this.categoriaRepository = categoriaRepository;
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

    public byte[] generarReportePlatillosPorHora(String fecha, int top, int horaInicio, int horaFin, Long categoriaId) {
        LocalDate dia = LocalDate.parse(fecha);
        LocalDateTime inicio = dia.atStartOfDay();
        LocalDateTime fin = dia.atTime(LocalTime.MAX);

        List<Pedido> pedidos = categoriaId != null
                ? pedidoRepository.findCerradosConProductosYCategoriaEntreFechas(inicio, fin, categoriaId)
                : pedidoRepository.findCerradosConProductosEntreFechas(inicio, fin);

        String catNombre = "";
        if (categoriaId != null) {
            catNombre = categoriaRepository.findById(categoriaId)
                    .map(c -> " - " + c.getNombre()).orElse("");
        }

        int numHoras = horaFin - horaInicio + 1;
        Map<Integer, Map<String, Long>> horaProductoCantidad = new TreeMap<>();
        Map<String, Long> totalPorProducto = new HashMap<>();

        for (int h = horaInicio; h <= horaFin; h++) horaProductoCantidad.put(h, new HashMap<>());

        for (Pedido p : pedidos) {
            int hora = p.getFecha().getHour();
            if (hora < horaInicio || hora > horaFin) continue;
            Map<String, Long> productos = horaProductoCantidad.get(hora);
            for (DetallePedido d : p.getDetalles()) {
                if (categoriaId != null && !d.getProducto().getCategoria().getId().equals(categoriaId)) continue;
                String nombre = d.getProducto().getNombre();
                long cant = d.getCantidad();
                productos.merge(nombre, cant, Long::sum);
                totalPorProducto.merge(nombre, cant, Long::sum);
            }
        }

        List<String> topPlatillos = totalPorProducto.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(top)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int hora = horaInicio; hora <= horaFin; hora++) {
            Map<String, Long> productos = horaProductoCantidad.get(hora);
            for (String platillo : topPlatillos) {
                long cantidad = productos.getOrDefault(platillo, 0L);
                dataset.addValue(cantidad, platillo, String.format("%02d", hora));
            }
        }

        Color bg = new Color(0xF5, 0xF5, 0xF5);
        JFreeChart chart = ChartFactory.createBarChart(
                "Platillos más vendidos por hora" + catNombre,
                "Hora",
                "Cantidad",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false
        );
        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(bg);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER));

        java.awt.Paint[] colores = {
                new Color(0xE5, 0x3E, 0x3E), new Color(0x34, 0x98, 0xDB), new Color(0x2E, 0xCC, 0x71),
                new Color(0xF3, 0x9C, 0x12), new Color(0x9B, 0x59, 0xB6), new Color(0x1A, 0xBC, 0x9C),
                new Color(0xE7, 0x4C, 0x3C), new Color(0x34, 0x95, 0xDB), new Color(0x2C, 0x3E, 0x50),
                new Color(0xD3, 0x54, 0x00)
        };
        for (int i = 0; i < topPlatillos.size(); i++) {
            renderer.setSeriesPaint(i, colores[i % colores.length]);
        }

        BufferedImage chartImage = chart.createBufferedImage(720, 380);
        byte[] chartBytes = imageToPng(chartImage);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "PLATILLOS MÁS VENDIDOS POR HORA" + catNombre);
        addField(doc, "Fecha", dia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        addField(doc, "Horario", horaInicio + ":00 a " + horaFin + ":00");
        addField(doc, "Top", String.valueOf(top));
        doc.add(Chunk.NEWLINE);

        Image chartImg = toImage(chartBytes);
        chartImg.setAlignment(Image.ALIGN_CENTER);
        chartImg.scaleToFit(doc.getPageSize().getWidth() - 40, 380);
        doc.add(chartImg);

        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1, 1});
        addCell(table, "Platillo", true);
        addCell(table, "Hora pico", true);
        addCell(table, "Total vendido", true);

        for (String platillo : topPlatillos) {
            long total = totalPorProducto.getOrDefault(platillo, 0L);
            String horaPico = horaProductoCantidad.entrySet().stream()
                    .max(Comparator.comparingLong(e -> e.getValue().getOrDefault(platillo, 0L)))
                    .map(e -> String.format("%02d:00", e.getKey()))
                    .orElse("-");
            addCell(table, platillo, false);
            addCell(table, horaPico, false);
            addCell(table, String.valueOf(total), false);
        }

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    public byte[] generarReporteKPIs(String fecha, Long categoriaId) {
        LocalDate dia = LocalDate.parse(fecha);
        LocalDateTime hoyInicio = dia.atStartOfDay();
        LocalDateTime hoyFin = dia.atTime(LocalTime.MAX);

        LocalDate lunes = dia.with(java.time.DayOfWeek.MONDAY);
        LocalDate domingo = dia.with(java.time.DayOfWeek.SUNDAY);
        LocalDateTime semanaInicio = lunes.atStartOfDay();
        LocalDateTime semanaFin = domingo.atTime(LocalTime.MAX);

        LocalDate mesInicio = dia.withDayOfMonth(1);
        LocalDate mesFin = dia.withDayOfMonth(dia.lengthOfMonth());
        LocalDateTime mesInicioDT = mesInicio.atStartOfDay();
        LocalDateTime mesFinDT = mesFin.atTime(LocalTime.MAX);

        LocalDateTime ultimos30Inicio = dia.minusDays(30).atStartOfDay();

        String catNombre = "";
        if (categoriaId != null) {
            catNombre = categoriaRepository.findById(categoriaId)
                    .map(c -> " (" + c.getNombre() + ")").orElse("");
        }

        double ventasHoy = 0, ventasSemana = 0, ventasMes = 0, ticketPromedio = 0;
        long pedidosHoy = 0;

        if (categoriaId == null) {
            ventasHoy = pedidoRepository.sumTotalBetweenFechas(hoyInicio, hoyFin);
            ventasSemana = pedidoRepository.sumTotalBetweenFechas(semanaInicio, semanaFin);
            ventasMes = pedidoRepository.sumTotalBetweenFechas(mesInicioDT, mesFinDT);
            pedidosHoy = pedidoRepository.countPedidosBetweenFechas(hoyInicio, hoyFin);
            double total30 = pedidoRepository.sumTotalBetweenFechas(ultimos30Inicio, hoyFin);
            long count30 = pedidoRepository.countPedidosBetweenFechas(ultimos30Inicio, hoyFin);
            ticketPromedio = count30 > 0 ? total30 / count30 : 0;
        } else {
            List<Pedido> kpiPedidos = pedidoRepository.findCerradosConProductosYCategoriaEntreFechas(hoyInicio, hoyFin, categoriaId);
            for (Pedido p : kpiPedidos) {
                for (DetallePedido d : p.getDetalles()) {
                    if (!d.getProducto().getCategoria().getId().equals(categoriaId)) continue;
                    double subtotal = d.getSubtotal();
                    ventasHoy += subtotal;
                    pedidosHoy++;
                }
            }
            kpiPedidos = pedidoRepository.findCerradosConProductosYCategoriaEntreFechas(semanaInicio, semanaFin, categoriaId);
            for (Pedido p : kpiPedidos) {
                for (DetallePedido d : p.getDetalles()) {
                    if (!d.getProducto().getCategoria().getId().equals(categoriaId)) continue;
                    ventasSemana += d.getSubtotal();
                }
            }
            kpiPedidos = pedidoRepository.findCerradosConProductosYCategoriaEntreFechas(mesInicioDT, mesFinDT, categoriaId);
            for (Pedido p : kpiPedidos) {
                for (DetallePedido d : p.getDetalles()) {
                    if (!d.getProducto().getCategoria().getId().equals(categoriaId)) continue;
                    ventasMes += d.getSubtotal();
                }
            }
            double total30cat = 0;
            long cnt30cat = 0;
            List<Pedido> ped30 = pedidoRepository.findCerradosConProductosYCategoriaEntreFechas(ultimos30Inicio, hoyFin, categoriaId);
            for (Pedido p : ped30) {
                for (DetallePedido d : p.getDetalles()) {
                    if (!d.getProducto().getCategoria().getId().equals(categoriaId)) continue;
                    total30cat += d.getSubtotal();
                    cnt30cat++;
                }
            }
            ticketPromedio = cnt30cat > 0 ? total30cat / cnt30cat : 0;
        }

        Map<String, Long> platilloCantidad = new HashMap<>();
        List<Pedido> pedidosHoyCompletos = categoriaId != null
                ? pedidoRepository.findCerradosConProductosYCategoriaEntreFechas(hoyInicio, hoyFin, categoriaId)
                : pedidoRepository.findCerradosConProductosEntreFechas(hoyInicio, hoyFin);
        for (Pedido p : pedidosHoyCompletos) {
            for (DetallePedido d : p.getDetalles()) {
                if (categoriaId != null && !d.getProducto().getCategoria().getId().equals(categoriaId)) continue;
                platilloCantidad.merge(d.getProducto().getNombre(), (long) d.getCantidad(), Long::sum);
            }
        }
        String platilloEstrella = platilloCantidad.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("-");

        LocalDate inicioSemanaGrafico = dia.minusDays(6);
        List<Pedido> pedidosSemana = pedidoRepository.findCerradosEntreFechas(
                inicioSemanaGrafico.atStartOfDay(), hoyFin);
        Map<LocalDate, Double> ventasPorDia = new LinkedHashMap<>();
        Map<LocalDate, Long> pedidosPorDia = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate d = dia.minusDays(i);
            ventasPorDia.put(d, 0.0);
            pedidosPorDia.put(d, 0L);
        }
        for (Pedido p : pedidosSemana) {
            LocalDate d = p.getFecha().toLocalDate();
            if (ventasPorDia.containsKey(d)) {
                ventasPorDia.merge(d, p.getTotal(), Double::sum);
                pedidosPorDia.merge(d, 1L, Long::sum);
            }
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<LocalDate, Double> entry : ventasPorDia.entrySet()) {
            dataset.addValue(entry.getValue(), "Ventas",
                    entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM")));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Ventas de los últimos 7 días", "Día", "Total ($)",
                dataset, PlotOrientation.VERTICAL, false, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0xF5, 0xF5, 0xF5));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, new Color(0x34, 0x98, 0xDB));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER));

        byte[] chartBytes = imageToPng(chart.createBufferedImage(680, 300));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "DASHBOARD DE INDICADORES" + catNombre);
        addField(doc, "Al", dia.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        doc.add(Chunk.NEWLINE);

        PdfPTable kpiTable = new PdfPTable(2);
        kpiTable.setWidthPercentage(100);
        kpiTable.setWidths(new float[]{1, 1});
        kpiTable.setSpacingBefore(5);
        kpiTable.setSpacingAfter(10);

        addKpiCell(kpiTable, "Ventas hoy", String.format("$%,.2f", ventasHoy));
        addKpiCell(kpiTable, "Ventas esta semana", String.format("$%,.2f", ventasSemana));
        addKpiCell(kpiTable, "Ventas este mes", String.format("$%,.2f", ventasMes));
        addKpiCell(kpiTable, "Pedidos hoy", String.valueOf(pedidosHoy));
        addKpiCell(kpiTable, "Ticket promedio (30d)", String.format("$%,.2f", ticketPromedio));
        addKpiCell(kpiTable, "Platillo estrella", platilloEstrella);
        doc.add(kpiTable);

        doc.add(Chunk.NEWLINE);
        Image chartImg = toImage(chartBytes);
        chartImg.setAlignment(Image.ALIGN_CENTER);
        chartImg.scaleToFit(doc.getPageSize().getWidth() - 40, 300);
        doc.add(chartImg);
        doc.add(Chunk.NEWLINE);

        PdfPTable detTable = new PdfPTable(3);
        detTable.setWidthPercentage(100);
        detTable.setWidths(new float[]{2, 2, 1});
        addCell(detTable, "Fecha", true);
        addCell(detTable, "Ventas", true);
        addCell(detTable, "Pedidos", true);
        for (int i = 6; i >= 0; i--) {
            LocalDate d = dia.minusDays(i);
            double v = ventasPorDia.getOrDefault(d, 0.0);
            long p = pedidosPorDia.getOrDefault(d, 0L);
            addCell(detTable, d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), false);
            addCell(detTable, String.format("$%,.2f", v), false);
            addCell(detTable, String.valueOf(p), false);
        }
        doc.add(detTable);

        doc.close();
        return out.toByteArray();
    }

    public byte[] generarReporteHorasPico(String fechaInicio, String fechaFin) {
        LocalDate inicioD = LocalDate.parse(fechaInicio);
        LocalDate finD = LocalDate.parse(fechaFin);
        LocalDateTime inicio = inicioD.atStartOfDay();
        LocalDateTime fin = finD.atTime(LocalTime.MAX);

        List<LocalDateTime> fechas = pedidoRepository.findFechasCerradasEntre(inicio, fin);

        long[][] conteo = new long[7][24];
        long[] totalPorDia = new long[7];
        long[] maxPorDia = new long[7];
        int[] horaPicoPorDia = new int[7];
        Arrays.fill(horaPicoPorDia, -1);
        long[] totalPorHora = new long[24];

        for (LocalDateTime f : fechas) {
            int dow = f.getDayOfWeek().getValue() % 7;
            int hour = f.getHour();
            conteo[dow][hour]++;
            totalPorDia[dow]++;
            totalPorHora[hour]++;
            if (conteo[dow][hour] > maxPorDia[dow]) {
                maxPorDia[dow] = conteo[dow][hour];
                horaPicoPorDia[dow] = hour;
            }
        }

        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int hour = 0; hour < 24; hour++) {
            long entreSemana = 0, finSemana = 0;
            for (int dow = 0; dow < 5; dow++) entreSemana += conteo[dow][hour];
            for (int dow = 5; dow < 7; dow++) finSemana += conteo[dow][hour];
            String label = String.format("%02d", hour);
            if (entreSemana > 0) dataset.addValue(entreSemana, "Entre semana", label);
            if (finSemana > 0) dataset.addValue(finSemana, "Fin de semana", label);
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Pedidos por hora del día", "Hora", "Pedidos",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0xF5, 0xF5, 0xF5));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, new Color(0x34, 0x98, 0xDB));
        renderer.setSeriesPaint(1, new Color(0xE7, 0x4C, 0x3C));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
        renderer.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER));

        byte[] chartBytes = imageToPng(chart.createBufferedImage(700, 320));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "HORAS PICO Y TRÁFICO");
        addField(doc, "Período", inicioD.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " a " +
                finD.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        addField(doc, "Total pedidos", String.valueOf(fechas.size()));
        doc.add(Chunk.NEWLINE);

        Image chartImg = toImage(chartBytes);
        chartImg.setAlignment(Image.ALIGN_CENTER);
        chartImg.scaleToFit(doc.getPageSize().getWidth() - 40, 320);
        doc.add(chartImg);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 1, 1, 1});
        addCell(table, "Día", true);
        addCell(table, "Hora pico", true);
        addCell(table, "Pedidos (pico)", true);
        addCell(table, "Total", true);
        long granTotal = 0;
        for (int dow = 0; dow < 7; dow++) {
            addCell(table, dias[dow], false);
            addCell(table, horaPicoPorDia[dow] >= 0 ? String.format("%02d:00", horaPicoPorDia[dow]) : "-", false);
            addCell(table, String.valueOf(maxPorDia[dow]), false);
            addCell(table, String.valueOf(totalPorDia[dow]), false);
            granTotal += totalPorDia[dow];
        }
        addCell(table, "TOTAL", true);
        addCell(table, "", true);
        addCell(table, "", true);
        addCell(table, String.valueOf(granTotal), true);

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    public byte[] generarReporteTicketPromedio(String fechaInicio, String fechaFin) {
        LocalDate inicioD = LocalDate.parse(fechaInicio);
        LocalDate finD = LocalDate.parse(fechaFin);
        LocalDateTime inicio = inicioD.atStartOfDay();
        LocalDateTime fin = finD.atTime(LocalTime.MAX);

        List<Pedido> pedidos = pedidoRepository.findCerradosEntreFechas(inicio, fin);

        long[] sumEntreSemana = new long[24];
        long[] cntEntreSemana = new long[24];
        long[] sumFinSemana = new long[24];
        long[] cntFinSemana = new long[24];
        long[] sumPorDia = new long[7];
        long[] cntPorDia = new long[7];
        Map<Integer, long[]> mesaSumCount = new HashMap<>();

        for (Pedido p : pedidos) {
            int hour = p.getFecha().getHour();
            int dow = p.getFecha().getDayOfWeek().getValue() % 7;
            long total = Math.round(p.getTotal() * 100);

            if (dow < 5) { sumEntreSemana[hour] += total; cntEntreSemana[hour]++; }
            else { sumFinSemana[hour] += total; cntFinSemana[hour]++; }
            sumPorDia[dow] += total;
            cntPorDia[dow]++;
            int numMesa = p.getMesa().getNumero();
            long[] sc = mesaSumCount.computeIfAbsent(numMesa, k -> new long[2]);
            sc[0] += total; sc[1]++;
        }

        long totalGlobalSum = 0, totalGlobalCnt = pedidos.size();

        DefaultCategoryDataset lineDataset = new DefaultCategoryDataset();
        for (int hour = 0; hour < 24; hour++) {
            String label = String.format("%02d", hour);
            if (cntEntreSemana[hour] > 0)
                lineDataset.addValue((double) sumEntreSemana[hour] / cntEntreSemana[hour] / 100, "Entre semana", label);
            if (cntFinSemana[hour] > 0)
                lineDataset.addValue((double) sumFinSemana[hour] / cntFinSemana[hour] / 100, "Fin de semana", label);
        }

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Ticket promedio por hora", "Hora", "Ticket promedio ($)",
                lineDataset, PlotOrientation.VERTICAL, true, true, false);
        lineChart.setBackgroundPaint(Color.WHITE);
        CategoryPlot linePlot = lineChart.getCategoryPlot();
        linePlot.setBackgroundPaint(new Color(0xF5, 0xF5, 0xF5));
        linePlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        linePlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        linePlot.setDomainGridlinesVisible(true);
        linePlot.setRangeGridlinesVisible(true);
        org.jfree.chart.renderer.category.LineAndShapeRenderer lineR =
                (org.jfree.chart.renderer.category.LineAndShapeRenderer) linePlot.getRenderer();
        lineR.setDefaultLinesVisible(true);
        lineR.setDefaultShapesVisible(true);
        lineR.setSeriesPaint(0, new Color(0x34, 0x98, 0xDB));
        lineR.setSeriesPaint(1, new Color(0xE7, 0x4C, 0x3C));

        String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
        for (int dow = 0; dow < 7; dow++) {
            if (cntPorDia[dow] > 0) {
                double avg = (double) sumPorDia[dow] / cntPorDia[dow] / 100;
                barDataset.addValue(avg, "Ticket promedio", dias[dow]);
            }
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Ticket promedio por día de la semana", "Día", "Ticket promedio ($)",
                barDataset, PlotOrientation.VERTICAL, false, true, false);
        barChart.setBackgroundPaint(Color.WHITE);
        CategoryPlot barPlot = barChart.getCategoryPlot();
        barPlot.setBackgroundPaint(new Color(0xF5, 0xF5, 0xF5));
        barPlot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        barPlot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        BarRenderer barR = (BarRenderer) barPlot.getRenderer();
        barR.setBarPainter(new StandardBarPainter());
        barR.setDrawBarOutline(false);
        barR.setSeriesPaint(0, new Color(0x2E, 0xCC, 0x71));
        barR.setDefaultItemLabelsVisible(true);
        barR.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        barR.setDefaultItemLabelFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
        barR.setDefaultPositiveItemLabelPosition(
                new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER));

        byte[] chart1Bytes = imageToPng(lineChart.createBufferedImage(700, 280));
        byte[] chart2Bytes = imageToPng(barChart.createBufferedImage(700, 240));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "TICKET PROMEDIO");
        addField(doc, "Período", inicioD.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " a " +
                finD.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        double ticketGlobal = totalGlobalCnt > 0 ? pedidos.stream().mapToDouble(Pedido::getTotal).sum() / totalGlobalCnt : 0;
        addField(doc, "Pedidos", String.valueOf(totalGlobalCnt));
        addField(doc, "Ticket prom. general", String.format("$%,.2f", ticketGlobal));
        doc.add(Chunk.NEWLINE);

        Image img1 = toImage(chart1Bytes);
        img1.setAlignment(Image.ALIGN_CENTER);
        img1.scaleToFit(doc.getPageSize().getWidth() - 40, 280);
        doc.add(img1);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);

        Image img2 = toImage(chart2Bytes);
        img2.setAlignment(Image.ALIGN_CENTER);
        img2.scaleToFit(doc.getPageSize().getWidth() - 40, 240);
        doc.add(img2);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 2});
        addCell(table, "Mesa", true);
        addCell(table, "Ticket promedio", true);
        addCell(table, "Pedidos", true);
        List<Map.Entry<Integer, long[]>> sortedMesas = new ArrayList<>(mesaSumCount.entrySet());
        sortedMesas.sort(Comparator.comparingInt(Map.Entry::getKey));
        for (Map.Entry<Integer, long[]> e : sortedMesas) {
            int num = e.getKey();
            long[] sc = e.getValue();
            double avg = sc[1] > 0 ? (double) sc[0] / sc[1] / 100 : 0;
            addCell(table, "Mesa " + num, false);
            addCell(table, String.format("$%,.2f", avg), false);
            addCell(table, String.valueOf(sc[1]), false);
        }

        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    public byte[] generarReporteTendenciaMensual(int anio) {
        int anioAnterior = anio - 1;
        LocalDateTime inicio = LocalDate.of(anioAnterior, 1, 1).atStartOfDay();
        LocalDateTime fin = LocalDate.of(anio, 12, 31).atTime(LocalTime.MAX);

        List<Object[]> rows = pedidoRepository.findFechasYTotalesEntre(inicio, fin);

        double[][] totales = new double[2][12];
        for (Object[] row : rows) {
            LocalDateTime fecha = (LocalDateTime) row[0];
            double total = ((Number) row[1]).doubleValue();
            int a = fecha.getYear();
            int m = fecha.getMonthValue() - 1;
            int idx = (a == anio) ? 0 : (a == anioAnterior ? 1 : -1);
            if (idx >= 0) totales[idx][m] += total;
        }

        String[] meses = {"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"};
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        double totalActual = 0, totalAnterior = 0;
        for (int m = 0; m < 12; m++) {
            totalActual += totales[0][m];
            totalAnterior += totales[1][m];
            dataset.addValue(totales[0][m], String.valueOf(anio), meses[m]);
            dataset.addValue(totales[1][m], String.valueOf(anioAnterior), meses[m]);
        }

        JFreeChart chart = ChartFactory.createLineChart(
                "Tendencia mensual de ventas", "Mes", "Ventas ($)",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0xF5, 0xF5, 0xF5));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        org.jfree.chart.renderer.category.LineAndShapeRenderer lineR =
                (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();
        lineR.setSeriesPaint(0, new Color(0x34, 0x98, 0xDB));
        lineR.setSeriesPaint(1, new Color(0xE7, 0x4C, 0x3C));
        lineR.setSeriesStroke(0, new java.awt.BasicStroke(2.5f));
        lineR.setSeriesStroke(1, new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 10f, new float[]{6, 4}, 0));

        byte[] chartBytes = imageToPng(chart.createBufferedImage(700, 300));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "TENDENCIA MENSUAL");
        addField(doc, "Años", anio + " vs " + anioAnterior);
        double crecimiento = totalAnterior > 0 ? (totalActual - totalAnterior) / totalAnterior * 100 : 0;
        addField(doc, "Total " + anio, String.format("$%,.2f", totalActual));
        addField(doc, "Total " + anioAnterior, String.format("$%,.2f", totalAnterior));
        addField(doc, "Crecimiento", String.format("%+.1f%%", crecimiento));
        doc.add(Chunk.NEWLINE);

        Image chartImg = toImage(chartBytes);
        chartImg.setAlignment(Image.ALIGN_CENTER);
        chartImg.scaleToFit(doc.getPageSize().getWidth() - 40, 300);
        doc.add(chartImg);
        doc.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2, 2, 2, 2});
        addCell(table, "Mes", true);
        addCell(table, String.valueOf(anio), true);
        addCell(table, String.valueOf(anioAnterior), true);
        addCell(table, "Dif.", true);
        addCell(table, "Var.", true);
        for (int m = 0; m < 12; m++) {
            double act = totales[0][m], ant = totales[1][m];
            double dif = act - ant;
            double variacion = ant > 0 ? (act - ant) / ant * 100 : 0;
            addCell(table, meses[m], false);
            addCell(table, String.format("$%,.0f", act), false);
            addCell(table, String.format("$%,.0f", ant), false);
            addCell(table, String.format("%+,.0f", dif), false);
            String varStr = String.format("%+.1f%%", variacion);
            addCell(table, varStr, false);
        }
        addCell(table, "TOTAL", true);
        addCell(table, String.format("$%,.0f", totalActual), true);
        addCell(table, String.format("$%,.0f", totalAnterior), true);
        addCell(table, String.format("%+,.0f", totalActual - totalAnterior), true);
        addCell(table, String.format("%+.1f%%", crecimiento), true);
        doc.add(table);
        doc.close();
        return out.toByteArray();
    }

    public byte[] generarReporteEstacionalidad(int anio, int top, String vista, int horaInicio, int horaFin, Long categoriaId) {
        LocalDateTime inicio = LocalDate.of(anio, 1, 1).atStartOfDay();
        LocalDateTime fin = LocalDate.of(anio, 12, 31).atTime(LocalTime.MAX);
        List<Pedido> pedidos = categoriaId != null
                ? pedidoRepository.findCerradosConProductosYCategoriaEntreFechas(inicio, fin, categoriaId)
                : pedidoRepository.findCerradosConProductosEntreFechas(inicio, fin);

        String catNombre = "";
        if (categoriaId != null) {
            catNombre = categoriaRepository.findById(categoriaId)
                    .map(c -> " - " + c.getNombre()).orElse("");
        }

        boolean esMensual = !"semanal".equalsIgnoreCase(vista);
        int categorias = esMensual ? 12 : 7;
        Map<Integer, Map<String, Long>> catProdCant = new LinkedHashMap<>();
        Map<String, Long> totalPorPlatillo = new HashMap<>();
        for (int c = 0; c < categorias; c++) catProdCant.put(c, new HashMap<>());

        for (Pedido p : pedidos) {
            int hora = p.getFecha().getHour();
            if (hora < horaInicio || hora > horaFin) continue;
            int cat = esMensual ? p.getFecha().getMonthValue() - 1 : p.getFecha().getDayOfWeek().getValue() % 7;
            Map<String, Long> m = catProdCant.get(cat);
            for (DetallePedido d : p.getDetalles()) {
                if (categoriaId != null && !d.getProducto().getCategoria().getId().equals(categoriaId)) continue;
                String nombre = d.getProducto().getNombre();
                long cant = d.getCantidad();
                m.merge(nombre, cant, Long::sum);
                totalPorPlatillo.merge(nombre, cant, Long::sum);
            }
        }

        List<String> topPlatillos = totalPorPlatillo.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(top).map(Map.Entry::getKey).collect(Collectors.toList());

        String[] etiquetas = esMensual
                ? new String[]{"Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic"}
                : new String[]{"Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo"};

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int c = 0; c < categorias; c++) {
            Map<String, Long> m = catProdCant.get(c);
            for (String platillo : topPlatillos) {
                long cant = m.getOrDefault(platillo, 0L);
                if (cant > 0) dataset.addValue(cant, platillo, etiquetas[c]);
            }
        }

        JFreeChart chart = ChartFactory.createStackedBarChart(
                (esMensual ? "Estacionalidad mensual" : "Estacionalidad semanal") + catNombre,
                esMensual ? "Mes" : "Día", "Cantidad vendida",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(new Color(0xF5, 0xF5, 0xF5));
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        Color[] colores = {
                new Color(0xE5, 0x3E, 0x3E), new Color(0x34, 0x98, 0xDB), new Color(0x2E, 0xCC, 0x71),
                new Color(0xF3, 0x9C, 0x12), new Color(0x9B, 0x59, 0xB6), new Color(0x1A, 0xBC, 0x9C),
                new Color(0xE7, 0x4C, 0x3C), new Color(0x2C, 0x3E, 0x50), new Color(0xD3, 0x54, 0x00),
                new Color(0x8E, 0x44, 0xAD)
        };
        org.jfree.chart.renderer.category.StackedBarRenderer renderer =
                (org.jfree.chart.renderer.category.StackedBarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        for (int i = 0; i < top; i++) renderer.setSeriesPaint(i, colores[i % colores.length]);

        byte[] chartBytes = imageToPng(chart.createBufferedImage(720, 340));

        long[] totalPorCat = new long[categorias];
        for (int c = 0; c < categorias; c++) {
            for (String plat : topPlatillos) totalPorCat[c] += catProdCant.get(c).getOrDefault(plat, 0L);
        }
        long totalGlobal = Arrays.stream(totalPorCat).sum();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(doc, out);
        doc.open();

        addHeader(doc, "ESTACIONALIDAD DE PLATILLOS" + catNombre);
        addField(doc, "Año", String.valueOf(anio));
        addField(doc, "Vista", esMensual ? "Mensual" : "Semanal");
        addField(doc, "Horario", horaInicio + ":00 a " + horaFin + ":00");
        addField(doc, "Top", String.valueOf(top));
        addField(doc, "Total platillos", String.valueOf(totalGlobal));
        doc.add(Chunk.NEWLINE);

        Image chartImg = toImage(chartBytes);
        chartImg.setAlignment(Image.ALIGN_CENTER);
        chartImg.scaleToFit(doc.getPageSize().getWidth() - 40, 340);
        doc.add(chartImg);
        doc.add(Chunk.NEWLINE);

        PdfPTable table1 = new PdfPTable(2);
        table1.setWidthPercentage(100);
        table1.setWidths(new float[]{3, 2});
        addCell(table1, esMensual ? "Mes" : "Día", true);
        addCell(table1, "Total", true);
        for (int c = 0; c < categorias; c++) {
            addCell(table1, etiquetas[c], false);
            addCell(table1, String.valueOf(totalPorCat[c]), false);
        }
        addCell(table1, "TOTAL", true);
        addCell(table1, String.valueOf(totalGlobal), true);
        doc.add(table1);
        doc.add(Chunk.NEWLINE);

        PdfPTable table2 = new PdfPTable(3);
        table2.setWidthPercentage(100);
        table2.setWidths(new float[]{3, 2, 3});
        addCell(table2, "Platillo", true);
        addCell(table2, "Total", true);
        addCell(table2, esMensual ? "Mes pico" : "Día pico", true);
        for (String platillo : topPlatillos) {
            long total = totalPorPlatillo.getOrDefault(platillo, 0L);
            String pico = catProdCant.entrySet().stream()
                    .max(Comparator.comparingLong(e -> e.getValue().getOrDefault(platillo, 0L)))
                    .map(e -> etiquetas[e.getKey()])
                    .orElse("-");
            addCell(table2, platillo, false);
            addCell(table2, String.valueOf(total), false);
            addCell(table2, pico, false);
        }
        doc.add(table2);
        doc.close();
        return out.toByteArray();
    }

    private byte[] imageToPng(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Error al generar imagen del grafico", e);
        }
    }

    private Image toImage(byte[] bytes) {
        try {
            return Image.getInstance(bytes);
        } catch (IOException | BadElementException e) {
            throw new RuntimeException("Error al crear imagen PDF", e);
        }
    }

    private void addKpiCell(PdfPTable table, String label, String value) throws DocumentException {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(0xDD, 0xDD, 0xDD));
        cell.setBackgroundColor(new Color(0xF8, 0xF9, 0xFA));
        Paragraph lbl = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(0x66, 0x66, 0x66)));
        Paragraph val = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0x2C, 0x3E, 0x50)));
        val.setLeading(22);
        cell.addElement(lbl);
        cell.addElement(val);
        table.addCell(cell);
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
