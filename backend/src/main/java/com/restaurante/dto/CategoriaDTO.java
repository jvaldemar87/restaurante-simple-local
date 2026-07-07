package com.restaurante.dto;

import javax.validation.constraints.NotBlank;

public class CategoriaDTO {

    private Long id;

    @NotBlank
    private String nombre;

    private Integer orden;

    public CategoriaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
