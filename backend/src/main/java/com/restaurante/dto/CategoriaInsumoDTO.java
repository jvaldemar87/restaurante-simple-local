package com.restaurante.dto;

import javax.validation.constraints.NotBlank;

public class CategoriaInsumoDTO {

    private Long id;

    @NotBlank
    private String nombre;

    public CategoriaInsumoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
