package com.restaurante.controller;

import com.restaurante.dto.CategoriaPagoDTO;
import com.restaurante.service.CategoriaPagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/categorias-pago")
public class CategoriaPagoController {

    private final CategoriaPagoService service;

    public CategoriaPagoController(CategoriaPagoService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<CategoriaPagoDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<CategoriaPagoDTO> create(@Valid @RequestBody CategoriaPagoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
