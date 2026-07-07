package com.restaurante.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String concepto;

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_pago_id", nullable = false)
    private CategoriaPago categoriaPago;

    @Column(name = "evidencia_imagen")
    private String evidenciaImagen;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    public Pago() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public Double getMonto() { return monto; }
    public void setMonto(Double monto) { this.monto = monto; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public CategoriaPago getCategoriaPago() { return categoriaPago; }
    public void setCategoriaPago(CategoriaPago categoriaPago) { this.categoriaPago = categoriaPago; }
    public String getEvidenciaImagen() { return evidenciaImagen; }
    public void setEvidenciaImagen(String evidenciaImagen) { this.evidenciaImagen = evidenciaImagen; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
