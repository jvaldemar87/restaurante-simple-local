package com.restaurante.repository;

import com.restaurante.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaIdOrderByNombreAsc(Long categoriaId);
    List<Producto> findByCategoriaIdAndDisponibleTrueOrderByNombreAsc(Long categoriaId);
    List<Producto> findByDisponibleTrueOrderByNombreAsc();
}
