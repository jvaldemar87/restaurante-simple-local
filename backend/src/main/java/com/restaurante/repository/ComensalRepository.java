package com.restaurante.repository;

import com.restaurante.model.Comensal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComensalRepository extends JpaRepository<Comensal, Long> {
    List<Comensal> findByMesaIdOrderByFechaRegistroAsc(Long mesaId);
}
