package com.restaurante.controller;

import com.restaurante.dto.PagoDTO;
import com.restaurante.service.ImagenService;
import com.restaurante.service.PagoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;
    private final ImagenService imagenService;

    public PagoController(PagoService pagoService, ImagenService imagenService) {
        this.pagoService = pagoService;
        this.imagenService = imagenService;
    }

    @GetMapping
    public ResponseEntity<List<PagoDTO>> getAll(@RequestParam(required = false) Long categoria,
                                                @RequestParam(required = false) Integer mes,
                                                @RequestParam(required = false) Integer anio) {
        if (categoria != null) {
            return ResponseEntity.ok(pagoService.findByCategoria(categoria, mes, anio));
        }
        return ResponseEntity.ok(pagoService.findAll(mes, anio));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PagoDTO> create(@Valid @RequestBody PagoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoDTO> update(@PathVariable Long id, @Valid @RequestBody PagoDTO dto) {
        return ResponseEntity.ok(pagoService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pagoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String path = imagenService.save(file);
        return ResponseEntity.ok(path);
    }
}
