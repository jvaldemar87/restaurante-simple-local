package com.restaurante.service;

import com.restaurante.dto.ComensalDTO;
import com.restaurante.model.Comensal;
import com.restaurante.model.Mesa;
import com.restaurante.repository.ComensalRepository;
import com.restaurante.repository.MesaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ComensalService {

    private final ComensalRepository comensalRepository;
    private final MesaRepository mesaRepository;

    public ComensalService(ComensalRepository comensalRepository, MesaRepository mesaRepository) {
        this.comensalRepository = comensalRepository;
        this.mesaRepository = mesaRepository;
    }

    public List<ComensalDTO> findByMesa(Long mesaId) {
        return comensalRepository.findByMesaIdOrderByFechaRegistroAsc(mesaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ComensalDTO findById(Long id) {
        Comensal comensal = comensalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comensal no encontrado: " + id));
        return toDTO(comensal);
    }

    @Transactional
    public ComensalDTO create(ComensalDTO dto) {
        Mesa mesa = mesaRepository.findById(dto.getMesaId())
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada: " + dto.getMesaId()));

        Comensal comensal = new Comensal();
        comensal.setNombre(dto.getNombre());
        comensal.setMesa(mesa);

        mesa.setEstado("OCUPADA");
        mesaRepository.save(mesa);

        return toDTO(comensalRepository.save(comensal));
    }

    @Transactional
    public void delete(Long id) {
        Comensal comensal = comensalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comensal no encontrado: " + id));

        Mesa mesa = comensal.getMesa();
        comensalRepository.delete(comensal);

        if (comensalRepository.findByMesaIdOrderByFechaRegistroAsc(mesa.getId()).isEmpty()) {
            mesa.setEstado("LIBRE");
            mesaRepository.save(mesa);
        }
    }

    private ComensalDTO toDTO(Comensal c) {
        ComensalDTO dto = new ComensalDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setMesaId(c.getMesa().getId());
        dto.setMesaNumero(c.getMesa().getNumero());
        dto.setFechaRegistro(c.getFechaRegistro().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        return dto;
    }
}
