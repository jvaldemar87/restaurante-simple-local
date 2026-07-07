package com.restaurante.controller;

import com.restaurante.dto.CategoriaInsumoDTO;
import com.restaurante.service.CategoriaInsumoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categorias-insumo")
public class CategoriaInsumoController {

    private final CategoriaInsumoService service;

    public CategoriaInsumoController(CategoriaInsumoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<CategoriaInsumoDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<CategoriaInsumoDTO> create(@Valid @RequestBody CategoriaInsumoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
