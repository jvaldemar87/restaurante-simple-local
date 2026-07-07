package com.restaurante.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class PagoDTO {

    private Long id;

    @NotBlank
    private String concepto;

    @NotNull @Positive
    private Double monto;

    private String fecha;

    @NotNull
    private Long categoriaPagoId;

    private String categoriaPagoNombre;

    private String evidenciaImagen;

    private String observaciones;

    public PagoDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public Long getCategoriaPagoId() { return categoriaPagoId; }
    public void setCategoriaPagoId(Long categoriaPagoId) { this.categoriaPagoId = categoriaPagoId; }
    public String getCategoriaPagoNombre() { return categoriaPagoNombre; }
    public void setCategoriaPagoNombre(String categoriaPagoNombre) { this.categoriaPagoNombre = categoriaPagoNombre; }
    public String getEvidenciaImagen() { return evidenciaImagen; }
    public void setEvidenciaImagen(String evidenciaImagen) { this.evidenciaImagen = evidenciaImagen; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
