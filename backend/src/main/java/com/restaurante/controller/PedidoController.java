package com.restaurante.controller;

import com.restaurante.dto.DetallePedidoDTO;
import com.restaurante.dto.PedidoDTO;
import com.restaurante.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public ResponseEntity<List<PedidoDTO>> getByComensal(@RequestParam Long comensalId) {
        return ResponseEntity.ok(pedidoService.findByComensal(comensalId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PedidoDTO> create(@RequestParam Long comensalId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.create(comensalId));
    }

    @PostMapping("/{id}/detalles")
    public ResponseEntity<DetallePedidoDTO> addProducto(@PathVariable Long id,
                                                         @Valid @RequestBody DetallePedidoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.addProducto(id, dto));
    }

    @DeleteMapping("/detalles/{detalleId}")
    public ResponseEntity<Void> removeProducto(@PathVariable Long detalleId) {
        pedidoService.removeProducto(detalleId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/cerrar")
    public ResponseEntity<PedidoDTO> cerrarPedido(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.cerrarPedido(id));
    }

    @PostMapping("/cerrar-mesa/{mesaId}")
    public ResponseEntity<Void> cerrarCuentaMesa(@PathVariable Long mesaId) {
        pedidoService.cerrarCuentaMesa(mesaId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pedidoService.deletePedido(id);
        return ResponseEntity.noContent().build();
    }
}
