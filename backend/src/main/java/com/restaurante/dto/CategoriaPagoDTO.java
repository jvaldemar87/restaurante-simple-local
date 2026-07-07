package com.restaurante.dto;

import javax.validation.constraints.NotBlank;

public class CategoriaPagoDTO {

    private Long id;

    @NotBlank
    private String nombre;

    public CategoriaPagoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
