package com.restaurante.controller;

import com.restaurante.dto.InsumoDTO;
import com.restaurante.service.InsumoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/insumos")
public class InsumoController {

    private final InsumoService insumoService;

    public InsumoController(InsumoService insumoService) {
        this.insumoService = insumoService;
    }

    @GetMapping
    public ResponseEntity<List<InsumoDTO>> getAll(@RequestParam(required = false) Long categoria,
                                                   @RequestParam(required = false) Integer mes,
                                                   @RequestParam(required = false) Integer anio) {
        if (categoria != null && mes != null && anio != null) {
            return ResponseEntity.ok(insumoService.findByCategoriaAndMes(categoria, mes, anio));
        }
        if (mes != null && anio != null) {
            return ResponseEntity.ok(insumoService.findByMes(mes, anio));
        }
        if (categoria != null) {
            return ResponseEntity.ok(insumoService.findByCategoria(categoria));
        }
        return ResponseEntity.ok(insumoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsumoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(insumoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<InsumoDTO> create(@Valid @RequestBody InsumoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(insumoService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InsumoDTO> update(@PathVariable Long id, @Valid @RequestBody InsumoDTO dto) {
        return ResponseEntity.ok(insumoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        insumoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
