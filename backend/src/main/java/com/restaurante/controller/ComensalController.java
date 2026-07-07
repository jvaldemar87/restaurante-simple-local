package com.restaurante.controller;

import com.restaurante.dto.ComensalDTO;
import com.restaurante.service.ComensalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/comensales")
public class ComensalController {

    private final ComensalService comensalService;

    public ComensalController(ComensalService comensalService) {
        this.comensalService = comensalService;
    }

    @GetMapping
    public ResponseEntity<List<ComensalDTO>> getByMesa(@RequestParam Long mesaId) {
        return ResponseEntity.ok(comensalService.findByMesa(mesaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComensalDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(comensalService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ComensalDTO> create(@Valid @RequestBody ComensalDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comensalService.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        comensalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
