package com.restaurante.service;

import com.restaurante.dto.DetallePedidoDTO;
import com.restaurante.dto.PedidoDTO;
import com.restaurante.model.*;
import com.restaurante.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ComensalRepository comensalRepository;
    private final ProductoRepository productoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final MesaRepository mesaRepository;
    private final HistorialPedidoRepository historialPedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ComensalRepository comensalRepository,
                         ProductoRepository productoRepository,
                         DetallePedidoRepository detallePedidoRepository,
                         MesaRepository mesaRepository,
                         HistorialPedidoRepository historialPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.comensalRepository = comensalRepository;
        this.productoRepository = productoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.mesaRepository = mesaRepository;
        this.historialPedidoRepository = historialPedidoRepository;
    }

    @PostConstruct
    @Transactional
    public void migrarMesaId() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        for (Pedido p : pedidos) {
            if (p.getMesa() == null && p.getComensal() != null) {
                p.setMesa(p.getComensal().getMesa());
                pedidoRepository.save(p);
            }
        }
    }

    public List<PedidoDTO> findByComensal(Long comensalId) {
        return pedidoRepository.findByComensalIdWithDetalles(comensalId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PedidoDTO findById(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + id));
        return toDTO(pedido);
    }

    @Transactional
    public PedidoDTO create(Long comensalId) {
        Comensal comensal = comensalRepository.findById(comensalId)
                .orElseThrow(() -> new RuntimeException("Comensal no encontrado: " + comensalId));

        Pedido pedido = new Pedido();
        pedido.setComensal(comensal);
        pedido.setMesa(comensal.getMesa());
        pedido.setEstado("ACTIVO");
        pedido.setTotal(0.0);

        return toDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public DetallePedidoDTO addProducto(Long pedidoId, DetallePedidoDTO dto) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        if ("CERRADO".equals(pedido.getEstado())) {
            throw new RuntimeException("No se puede modificar un pedido cerrado");
        }

        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + dto.getProductoId()));

        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(dto.getCantidad() != null ? dto.getCantidad() : 1);
        detalle.setPrecioUnitario(producto.getPrecio());
        detalle.calcularSubtotal();

        detalle = detallePedidoRepository.save(detalle);

        recalcularTotal(pedido);

        return toDetalleDTO(detalle);
    }

    @Transactional
    public void removeProducto(Long detalleId) {
        DetallePedido detalle = detallePedidoRepository.findById(detalleId)
                .orElseThrow(() -> new RuntimeException("Detalle no encontrado: " + detalleId));

        Pedido pedido = detalle.getPedido();
        if ("CERRADO".equals(pedido.getEstado())) {
            throw new RuntimeException("No se puede modificar un pedido cerrado");
        }

        pedido.getDetalles().remove(detalle);
        detallePedidoRepository.delete(detalle);
        recalcularTotal(pedido);
    }

    @Transactional
    public PedidoDTO cerrarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        pedido.setEstado("COMIENDO");
        recalcularTotal(pedido);

        return toDTO(pedidoRepository.save(pedido));
    }

    @Transactional
    public void cerrarCuentaMesa(Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + mesaId));

        List<Comensal> comensales = comensalRepository.findByMesaIdOrderByFechaRegistroAsc(mesaId);
        int mesaNumero = mesa.getNumero();

        for (Comensal comensal : comensales) {
            List<Pedido> pedidos = pedidoRepository.findByComensalIdAndEstadoAbierto(comensal.getId());
            for (Pedido pedido : pedidos) {
                pedido.setEstado("CERRADO");
                recalcularTotal(pedido);

                for (DetallePedido d : pedido.getDetalles()) {
                    HistorialPedido h = new HistorialPedido();
                    h.setNombreComensal(comensal.getNombre());
                    h.setNombreProducto(d.getProducto().getNombre());
                    h.setCantidad(d.getCantidad());
                    h.setPrecioUnitario(d.getPrecioUnitario());
                    h.setSubtotal(d.getSubtotal());
                    h.setMesaNumero(mesaNumero);
                    h.setFechaPedido(pedido.getFecha());
                    historialPedidoRepository.save(h);
                }

                pedido.setComensal(null);
                pedidoRepository.save(pedido);
            }
        }

        comensalRepository.deleteAll(comensales);
        mesa.setEstado("LIBRE");
        mesaRepository.save(mesa);
    }

    @Transactional
    public void deletePedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));
        pedidoRepository.delete(pedido);
    }

    private void recalcularTotal(Pedido pedido) {
        List<DetallePedido> detalles = pedido.getDetalles();
        double total = detalles.stream()
                .filter(d -> d.getPedido().getId().equals(pedido.getId()))
                .mapToDouble(DetallePedido::getSubtotal)
                .sum();
        pedido.setTotal(total);
        pedidoRepository.save(pedido);
    }

    private PedidoDTO toDTO(Pedido p) {
        PedidoDTO dto = new PedidoDTO();
        dto.setId(p.getId());
        dto.setComensalId(p.getComensal() != null ? p.getComensal().getId() : null);
        dto.setComensalNombre(p.getComensal() != null ? p.getComensal().getNombre() : null);
        dto.setFecha(p.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dto.setEstado(p.getEstado());
        dto.setTotal(p.getTotal());
        dto.setDetalles(p.getDetalles().stream().map(this::toDetalleDTO).collect(Collectors.toList()));
        return dto;
    }

    private DetallePedidoDTO toDetalleDTO(DetallePedido d) {
        DetallePedidoDTO dto = new DetallePedidoDTO();
        dto.setId(d.getId());
        dto.setPedidoId(d.getPedido().getId());
        dto.setProductoId(d.getProducto().getId());
        dto.setProductoNombre(d.getProducto().getNombre());
        dto.setCantidad(d.getCantidad());
        dto.setPrecioUnitario(d.getPrecioUnitario());
        dto.setSubtotal(d.getSubtotal());
        return dto;
    }
}

