package com.restaurante.dto;

import java.util.List;

public class ComandaDTO {

    private Long pedidoId;
    private String comensalNombre;
    private Integer mesaNumero;
    private Long mesaId;
    private String fechaComanda;
    private List<ComandaItemDTO> items;

    public ComandaDTO() {}

    public Long getPedidoId() { return pedidoId; }
    public void setPedidoId(Long pedidoId) { this.pedidoId = pedidoId; }
    public String getComensalNombre() { return comensalNombre; }
    public void setComensalNombre(String comensalNombre) { this.comensalNombre = comensalNombre; }
    public Integer getMesaNumero() { return mesaNumero; }
    public void setMesaNumero(Integer mesaNumero) { this.mesaNumero = mesaNumero; }
    public Long getMesaId() { return mesaId; }
    public void setMesaId(Long mesaId) { this.mesaId = mesaId; }
    public String getFechaComanda() { return fechaComanda; }
    public void setFechaComanda(String fechaComanda) { this.fechaComanda = fechaComanda; }
    public List<ComandaItemDTO> getItems() { return items; }
    public void setItems(List<ComandaItemDTO> items) { this.items = items; }
}
