package com.restaurante.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorias_insumo")
public class CategoriaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @OneToMany(mappedBy = "categoriaInsumo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Insumo> insumos = new ArrayList<>();

    public CategoriaInsumo() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public List<Insumo> getInsumos() { return insumos; }
    public void setInsumos(List<Insumo> insumos) { this.insumos = insumos; }
}
