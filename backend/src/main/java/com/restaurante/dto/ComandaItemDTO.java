package com.restaurante.dto;

public class ComandaItemDTO {

    private Long productoId;
    private String productoNombre;
    private Integer cantidadTotal;

    public ComandaItemDTO() {}

    public ComandaItemDTO(Long productoId, String productoNombre, Integer cantidadTotal) {
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidadTotal = cantidadTotal;
    }

    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public Integer getCantidadTotal() { return cantidadTotal; }
    public void setCantidadTotal(Integer cantidadTotal) { this.cantidadTotal = cantidadTotal; }
}
