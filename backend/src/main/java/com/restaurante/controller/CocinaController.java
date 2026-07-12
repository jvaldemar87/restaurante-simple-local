package com.restaurante.controller;

import com.restaurante.dto.ComandaDTO;
import com.restaurante.service.CocinaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cocina")
public class CocinaController {

    private final CocinaService cocinaService;

    public CocinaController(CocinaService cocinaService) {
        this.cocinaService = cocinaService;
    }

    @GetMapping("/comandas")
    public ResponseEntity<List<ComandaDTO>> getComandas() {
        return ResponseEntity.ok(cocinaService.getComandasPendientes());
    }

    @PutMapping("/comandas/{pedidoId}/entregar")
    public ResponseEntity<ComandaDTO> entregarComanda(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(cocinaService.entregarComanda(pedidoId));
    }
}
