package com.restaurante.service;

import com.restaurante.dto.MesaDTO;
import com.restaurante.model.Comensal;
import com.restaurante.model.Mesa;
import com.restaurante.repository.ComensalRepository;
import com.restaurante.repository.MesaRepository;
import com.restaurante.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class MesaService {

    private final MesaRepository mesaRepository;
    private final ComensalRepository comensalRepository;
    private final PedidoRepository pedidoRepository;

    public MesaService(MesaRepository mesaRepository, ComensalRepository comensalRepository,
                       PedidoRepository pedidoRepository) {
        this.mesaRepository = mesaRepository;
        this.comensalRepository = comensalRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<MesaDTO> findAll() {
        return mesaRepository.findAllByOrderByNumeroAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public MesaDTO findById(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + id));
        return toDTO(mesa);
    }

    public MesaDTO create(MesaDTO dto) {
        Mesa mesa = new Mesa();
        mesa.setNumero(dto.getNumero());
        mesa.setCapacidad(dto.getCapacidad() != null ? dto.getCapacidad() : 4);
        mesa.setEstado("LIBRE");
        return toDTO(mesaRepository.save(mesa));
    }

    public MesaDTO update(Long id, MesaDTO dto) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + id));
        mesa.setNumero(dto.getNumero());
        if (dto.getCapacidad() != null) mesa.setCapacidad(dto.getCapacidad());
        if (dto.getEstado() != null) mesa.setEstado(dto.getEstado());
        return toDTO(mesaRepository.save(mesa));
    }

    public void delete(Long id) {
        if (!mesaRepository.existsById(id)) {
            throw new RuntimeException("Mesa no encontrada: " + id);
        }
        mesaRepository.deleteById(id);
    }

    private MesaDTO toDTO(Mesa m) {
        MesaDTO dto = new MesaDTO();
        dto.setId(m.getId());
        dto.setNumero(m.getNumero());
        dto.setEstado(m.getEstado());
        dto.setCapacidad(m.getCapacidad());
        List<Comensal> comensales = comensalRepository.findByMesaIdOrderByFechaRegistroAsc(m.getId());
        int activos = 0;
        for (Comensal c : comensales) {
            if (!pedidoRepository.findByComensalIdAndEstadoAbierto(c.getId()).isEmpty()) {
                activos++;
            }
        }
        dto.setComensalesCount(activos);
        return dto;
    }
}
