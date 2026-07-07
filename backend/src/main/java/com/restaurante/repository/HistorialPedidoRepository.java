package com.restaurante.repository;

import com.restaurante.model.HistorialPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistorialPedidoRepository extends JpaRepository<HistorialPedido, Long> {
}
