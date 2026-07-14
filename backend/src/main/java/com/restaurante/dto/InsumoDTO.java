package com.restaurante.dto;

import com.restaurante.validation.NotFuture;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class InsumoDTO {

    private Long id;

    @NotBlank
    private String nombre;

    @NotNull @Positive
    private Double cantidad;

    private String unidad = "pza";

    @NotFuture
    private String fechaIngreso;

    @NotNull @Positive
    private Double precioUnitario;

    @NotNull
    private Long categoriaInsumoId;

    private String categoriaInsumoNombre;

    public InsumoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public String getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(String fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
    public Long getCategoriaInsumoId() { return categoriaInsumoId; }
    public void setCategoriaInsumoId(Long categoriaInsumoId) { this.categoriaInsumoId = categoriaInsumoId; }
    public String getCategoriaInsumoNombre() { return categoriaInsumoNombre; }
    public void setCategoriaInsumoNombre(String categoriaInsumoNombre) { this.categoriaInsumoNombre = categoriaInsumoNombre; }
}
