package com.restaurante.repository;

import com.restaurante.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    List<Mesa> findAllByOrderByNumeroAsc();
}
