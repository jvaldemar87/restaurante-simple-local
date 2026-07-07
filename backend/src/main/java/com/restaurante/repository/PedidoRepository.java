package com.restaurante.repository;

import com.restaurante.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByComensalIdOrderByFechaAsc(Long comensalId);
    List<Pedido> findByComensalIdAndEstado(Long comensalId, String estado);
    List<Pedido> findByMesaIdAndEstado(Long mesaId, String estado);

    @Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.detalles WHERE p.comensal.id = :comensalId AND p.estado = :estado")
    List<Pedido> findByComensalIdAndEstadoWithDetalles(@Param("comensalId") Long comensalId, @Param("estado") String estado);

    @Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.detalles WHERE p.comensal.id = :comensalId ORDER BY p.fecha ASC")
    List<Pedido> findByComensalIdWithDetalles(@Param("comensalId") Long comensalId);

    @Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.detalles WHERE p.mesa.id = :mesaId")
    List<Pedido> findByMesaIdWithDetalles(@Param("mesaId") Long mesaId);

    @Query("SELECT DISTINCT p FROM Pedido p JOIN p.detalles d WHERE p.mesa.id = :mesaId AND p.total > 0 AND p.estado <> 'CERRADO'")
    List<Pedido> findByMesaIdWithDetallesAndEstadoAbierto(@Param("mesaId") Long mesaId);

    @Query("SELECT p FROM Pedido p WHERE p.comensal.id = :comensalId AND p.estado <> 'CERRADO'")
    List<Pedido> findByComensalIdAndEstadoAbierto(@Param("comensalId") Long comensalId);

    @Query("SELECT DISTINCT p FROM Pedido p JOIN FETCH p.mesa JOIN FETCH p.detalles WHERE p.estado = 'CERRADO' AND p.fecha >= :inicio AND p.fecha <= :fin ORDER BY p.fecha ASC")
    List<Pedido> findCerradosEntreFechas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
