package com.restaurante.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ComensalDTO {

    private Long id;

    @NotBlank
    private String nombre;

    @NotNull
    private Long mesaId;

    private Integer mesaNumero;

    private String fechaRegistro;

    public ComensalDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Long getMesaId() { return mesaId; }
    public void setMesaId(Long mesaId) { this.mesaId = mesaId; }
    public Integer getMesaNumero() { return mesaNumero; }
    public void setMesaNumero(Integer mesaNumero) { this.mesaNumero = mesaNumero; }
    public String getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(String fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
