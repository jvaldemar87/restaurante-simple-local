package com.restaurante.controller;

import com.restaurante.dto.MesaDTO;
import com.restaurante.service.MesaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    public ResponseEntity<List<MesaDTO>> getAll() {
        return ResponseEntity.ok(mesaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MesaDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(mesaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MesaDTO> create(@Valid @RequestBody MesaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MesaDTO> update(@PathVariable Long id, @Valid @RequestBody MesaDTO dto) {
        return ResponseEntity.ok(mesaService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        mesaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
