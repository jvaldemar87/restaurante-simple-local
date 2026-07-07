package com.restaurante.repository;

import com.restaurante.model.CategoriaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaInsumoRepository extends JpaRepository<CategoriaInsumo, Long> {
}
