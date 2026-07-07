package com.restaurante.service;

import com.restaurante.dto.PagoDTO;
import com.restaurante.model.CategoriaPago;
import com.restaurante.model.Pago;
import com.restaurante.repository.CategoriaPagoRepository;
import com.restaurante.repository.PagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PagoService {

    private final PagoRepository pagoRepository;
    private final CategoriaPagoRepository categoriaPagoRepository;

    public PagoService(PagoRepository pagoRepository, CategoriaPagoRepository categoriaPagoRepository) {
        this.pagoRepository = pagoRepository;
        this.categoriaPagoRepository = categoriaPagoRepository;
    }

    public List<PagoDTO> findAll(Integer mes, Integer anio) {
        if (mes != null && anio != null) {
            YearMonth ym = YearMonth.of(anio, mes);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);
            return pagoRepository.findByFechaBetweenOrderByFechaDesc(start, end).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        return pagoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<PagoDTO> findByCategoria(Long categoriaId, Integer mes, Integer anio) {
        if (mes != null && anio != null) {
            YearMonth ym = YearMonth.of(anio, mes);
            LocalDateTime start = ym.atDay(1).atStartOfDay();
            LocalDateTime end = ym.atEndOfMonth().atTime(LocalTime.MAX);
            return pagoRepository.findByCategoriaPagoIdAndFechaBetweenOrderByFechaDesc(categoriaId, start, end).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        return pagoRepository.findByCategoriaPagoIdOrderByFechaDesc(categoriaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PagoDTO findById(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + id));
        return toDTO(pago);
    }

    @Transactional
    public PagoDTO create(PagoDTO dto) {
        CategoriaPago categoria = categoriaPagoRepository.findById(dto.getCategoriaPagoId())
                .orElseThrow(() -> new RuntimeException("Categoria de pago no encontrada: " + dto.getCategoriaPagoId()));

        Pago pago = new Pago();
        pago.setConcepto(dto.getConcepto());
        pago.setMonto(dto.getMonto());
        pago.setCategoriaPago(categoria);
        pago.setObservaciones(dto.getObservaciones());
        pago.setEvidenciaImagen(dto.getEvidenciaImagen());

        return toDTO(pagoRepository.save(pago));
    }

    @Transactional
    public PagoDTO update(Long id, PagoDTO dto) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + id));

        pago.setConcepto(dto.getConcepto());
        pago.setMonto(dto.getMonto());
        if (dto.getObservaciones() != null) pago.setObservaciones(dto.getObservaciones());
        pago.setEvidenciaImagen(dto.getEvidenciaImagen());

        if (dto.getCategoriaPagoId() != null) {
            CategoriaPago categoria = categoriaPagoRepository.findById(dto.getCategoriaPagoId())
                    .orElseThrow(() -> new RuntimeException("Categoria de pago no encontrada: " + dto.getCategoriaPagoId()));
            pago.setCategoriaPago(categoria);
        }

        return toDTO(pagoRepository.save(pago));
    }

    @Transactional
    public void delete(Long id) {
        if (!pagoRepository.existsById(id)) {
            throw new RuntimeException("Pago no encontrado: " + id);
        }
        pagoRepository.deleteById(id);
    }

    private PagoDTO toDTO(Pago p) {
        PagoDTO dto = new PagoDTO();
        dto.setId(p.getId());
        dto.setConcepto(p.getConcepto());
        dto.setMonto(p.getMonto());
        dto.setFecha(p.getFecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dto.setCategoriaPagoId(p.getCategoriaPago().getId());
        dto.setCategoriaPagoNombre(p.getCategoriaPago().getNombre());
        dto.setEvidenciaImagen(p.getEvidenciaImagen());
        dto.setObservaciones(p.getObservaciones());
        return dto;
    }
}
