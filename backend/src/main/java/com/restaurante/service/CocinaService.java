package com.restaurante.service;

import com.restaurante.dto.ComandaDTO;
import com.restaurante.dto.ComandaItemDTO;
import com.restaurante.model.DetallePedido;
import com.restaurante.model.Pedido;
import com.restaurante.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CocinaService {

    private final PedidoRepository pedidoRepository;

    public CocinaService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public List<ComandaDTO> getComandasPendientes() {
        List<Pedido> pedidos = pedidoRepository.findByEstadoAndEntregadoFalseOrderByFechaComandaAsc();
        return pedidos.stream().map(this::toComandaDTO).collect(Collectors.toList());
    }

    @Transactional
    public ComandaDTO entregarComanda(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));
        pedido.setEntregado(true);
        pedido = pedidoRepository.save(pedido);
        return toComandaDTO(pedido);
    }

    private ComandaDTO toComandaDTO(Pedido p) {
        ComandaDTO dto = new ComandaDTO();
        dto.setPedidoId(p.getId());
        dto.setComensalNombre(p.getComensal() != null ? p.getComensal().getNombre() : "(sin comensal)");
        dto.setMesaNumero(p.getMesa().getNumero());
        dto.setMesaId(p.getMesa().getId());
        dto.setFechaComanda(p.getFechaComanda() != null
                ? p.getFechaComanda().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : p.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        Map<Long, List<DetallePedido>> agrupado = p.getDetalles().stream()
                .collect(Collectors.groupingBy(d -> d.getProducto().getId()));

        List<ComandaItemDTO> items = agrupado.entrySet().stream()
                .map(entry -> {
                    int cantidadTotal = entry.getValue().stream()
                            .mapToInt(DetallePedido::getCantidad)
                            .sum();
                    String nombre = entry.getValue().get(0).getProducto().getNombre();
                    return new ComandaItemDTO(entry.getKey(), nombre, cantidadTotal);
                })
                .collect(Collectors.toList());

        dto.setItems(items);
        return dto;
    }
}
