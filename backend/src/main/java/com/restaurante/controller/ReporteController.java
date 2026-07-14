package com.restaurante.controller;

import com.restaurante.service.ReporteService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/ticket/{mesaId}")
    public ResponseEntity<byte[]> getTicket(@PathVariable Long mesaId) {
        byte[] pdf = reporteService.generarTicketMesa(mesaId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ticket-mesa-" + mesaId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/comanda/{mesaId}")
    public ResponseEntity<byte[]> getComanda(@PathVariable Long mesaId) {
        byte[] pdf = reporteService.generarComandaMesa(mesaId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=comanda-mesa-" + mesaId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/ventas")
    public ResponseEntity<byte[]> getVentas(@RequestParam String fechaInicio, @RequestParam String fechaFin) {
        byte[] pdf = reporteService.generarReporteVentas(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-ventas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/ventas-resumen")
    public ResponseEntity<byte[]> getVentasResumen(@RequestParam String fechaInicio, @RequestParam String fechaFin) {
        byte[] pdf = reporteService.generarReporteVentasResumen(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-ventas-resumen.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/insumos")
    public ResponseEntity<byte[]> getInsumos(@RequestParam(required = false) Long categoria,
                                              @RequestParam(required = false) Integer mes,
                                              @RequestParam(required = false) Integer anio) {
        byte[] pdf = reporteService.generarReporteInsumos(categoria, mes, anio);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-insumos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/ticket-promedio")
    public ResponseEntity<byte[]> getTicketPromedio(@RequestParam String fechaInicio, @RequestParam String fechaFin) {
        byte[] pdf = reporteService.generarReporteTicketPromedio(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ticket-promedio.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/tendencia-mensual")
    public ResponseEntity<byte[]> getTendenciaMensual(@RequestParam int anio) {
        byte[] pdf = reporteService.generarReporteTendenciaMensual(anio);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=tendencia-mensual.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/estacionalidad")
    public ResponseEntity<byte[]> getEstacionalidad(@RequestParam int anio,
                                                     @RequestParam(defaultValue = "10") int top,
                                                     @RequestParam(defaultValue = "mensual") String vista,
                                                     @RequestParam(defaultValue = "0") int horaInicio,
                                                     @RequestParam(defaultValue = "23") int horaFin,
                                                     @RequestParam(required = false) Long categoria) {
        byte[] pdf = reporteService.generarReporteEstacionalidad(anio, top, vista, horaInicio, horaFin, categoria);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=estacionalidad.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/horas-pico")
    public ResponseEntity<byte[]> getHorasPico(@RequestParam String fechaInicio, @RequestParam String fechaFin) {
        byte[] pdf = reporteService.generarReporteHorasPico(fechaInicio, fechaFin);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=horas-pico.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/kpis")
    public ResponseEntity<byte[]> getKPIs(@RequestParam String fecha,
                                           @RequestParam(required = false) Long categoria) {
        byte[] pdf = reporteService.generarReporteKPIs(fecha, categoria);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-kpis.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/platillos-por-hora")
    public ResponseEntity<byte[]> getPlatillosPorHora(@RequestParam String fecha,
                                                       @RequestParam(defaultValue = "10") int top,
                                                       @RequestParam(defaultValue = "0") int horaInicio,
                                                       @RequestParam(defaultValue = "23") int horaFin,
                                                       @RequestParam(required = false) Long categoria) {
        byte[] pdf = reporteService.generarReportePlatillosPorHora(fecha, top, horaInicio, horaFin, categoria);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=platillos-por-hora.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/pagos")
    public ResponseEntity<byte[]> getPagos(@RequestParam(required = false) Long categoria,
                                           @RequestParam(required = false) Integer mes,
                                           @RequestParam(required = false) Integer anio) {
        byte[] pdf = reporteService.generarReportePagos(categoria, mes, anio);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=reporte-pagos.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
