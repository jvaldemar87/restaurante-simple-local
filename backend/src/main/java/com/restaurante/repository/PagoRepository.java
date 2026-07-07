package com.restaurante.repository;

import com.restaurante.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByCategoriaPagoIdOrderByFechaDesc(Long categoriaPagoId);
    List<Pago> findByFechaBetweenOrderByFechaDesc(LocalDateTime start, LocalDateTime end);
    List<Pago> findByCategoriaPagoIdAndFechaBetweenOrderByFechaDesc(Long categoriaPagoId, LocalDateTime start, LocalDateTime end);
}
