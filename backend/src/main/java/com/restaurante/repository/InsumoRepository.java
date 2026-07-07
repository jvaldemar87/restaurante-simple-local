package com.restaurante.repository;

import com.restaurante.model.Insumo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface InsumoRepository extends JpaRepository<Insumo, Long> {
    List<Insumo> findByCategoriaInsumoIdOrderByNombreAsc(Long categoriaInsumoId);
    List<Insumo> findByFechaIngresoBetweenOrderByNombreAsc(LocalDateTime inicio, LocalDateTime fin);
    List<Insumo> findByCategoriaInsumoIdAndFechaIngresoBetweenOrderByNombreAsc(Long categoriaInsumoId, LocalDateTime inicio, LocalDateTime fin);
}
