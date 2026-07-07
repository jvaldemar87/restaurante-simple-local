package com.restaurante.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class MesaDTO {

    private Long id;

    @NotNull @Positive
    private Integer numero;

    private String estado;

    private Integer capacidad = 4;

    private Integer comensalesCount = 0;

    public MesaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getNumero() { return numero; }
    public void setNumero(Integer numero) { this.numero = numero; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }
    public Integer getComensalesCount() { return comensalesCount; }
    public void setComensalesCount(Integer comensalesCount) { this.comensalesCount = comensalesCount; }
}
