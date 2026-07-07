package com.restaurante.dto;

import javax.validation.constraints.NotNull;
import java.util.List;

public class PedidoDTO {

    private Long id;

    @NotNull
    private Long comensalId;

    private String comensalNombre;

    private String fecha;

    private String estado;

    private Double total;

    private List<DetallePedidoDTO> detalles;

    public PedidoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getComensalId() { return comensalId; }
    public void setComensalId(Long comensalId) { this.comensalId = comensalId; }
    public String getComensalNombre() { return comensalNombre; }
    public void setComensalNombre(String comensalNombre) { this.comensalNombre = comensalNombre; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public List<DetallePedidoDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetallePedidoDTO> detalles) { this.detalles = detalles; }
}
