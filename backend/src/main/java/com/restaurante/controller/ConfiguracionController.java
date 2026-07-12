package com.restaurante.controller;

import com.restaurante.service.ConfiguracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping("/tiempo-tolerancia")
    public ResponseEntity<Map<String, Integer>> getTiempoTolerancia() {
        int minutos = configuracionService.getTiempoToleranciaMinutos();
        return ResponseEntity.ok(Map.of("minutos", minutos));
    }

    @PutMapping("/tiempo-tolerancia")
    public ResponseEntity<Map<String, Integer>> updateTiempoTolerancia(@RequestBody Map<String, Integer> body) {
        int minutos = body.getOrDefault("minutos", 30);
        configuracionService.updateTiempoToleranciaMinutos(minutos);
        return ResponseEntity.ok(Map.of("minutos", minutos));
    }

    @GetMapping("/alerta-intervalo")
    public ResponseEntity<Map<String, Integer>> getAlertaIntervalo() {
        int minutos = configuracionService.getAlertaIntervaloMinutos();
        return ResponseEntity.ok(Map.of("minutos", minutos));
    }

    @PutMapping("/alerta-intervalo")
    public ResponseEntity<Map<String, Integer>> updateAlertaIntervalo(@RequestBody Map<String, Integer> body) {
        int minutos = body.getOrDefault("minutos", 5);
        configuracionService.updateAlertaIntervaloMinutos(minutos);
        return ResponseEntity.ok(Map.of("minutos", minutos));
    }
}
