package com.restaurante.service;

import com.restaurante.dto.InsumoDTO;
import com.restaurante.model.CategoriaInsumo;
import com.restaurante.model.Insumo;
import com.restaurante.repository.CategoriaInsumoRepository;
import com.restaurante.repository.InsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InsumoService {

    private final InsumoRepository insumoRepository;
    private final CategoriaInsumoRepository categoriaInsumoRepository;

    public InsumoService(InsumoRepository insumoRepository, CategoriaInsumoRepository categoriaInsumoRepository) {
        this.insumoRepository = insumoRepository;
        this.categoriaInsumoRepository = categoriaInsumoRepository;
    }

    public List<InsumoDTO> findAll() {
        return insumoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<InsumoDTO> findByCategoria(Long categoriaId) {
        return insumoRepository.findByCategoriaInsumoIdOrderByNombreAsc(categoriaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public InsumoDTO findById(Long id) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + id));
        return toDTO(insumo);
    }

    public List<InsumoDTO> findByMes(Integer mes, Integer anio) {
        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return insumoRepository.findByFechaIngresoBetweenOrderByNombreAsc(
                inicio.atStartOfDay(), fin.atTime(LocalTime.MAX)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<InsumoDTO> findByCategoriaAndMes(Long categoriaId, Integer mes, Integer anio) {
        LocalDate inicio = LocalDate.of(anio, mes, 1);
        LocalDate fin = inicio.withDayOfMonth(inicio.lengthOfMonth());
        return insumoRepository.findByCategoriaInsumoIdAndFechaIngresoBetweenOrderByNombreAsc(
                categoriaId, inicio.atStartOfDay(), fin.atTime(LocalTime.MAX)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public InsumoDTO create(InsumoDTO dto) {
        CategoriaInsumo categoria = categoriaInsumoRepository.findById(dto.getCategoriaInsumoId())
                .orElseThrow(() -> new RuntimeException("Categoria de insumo no encontrada: " + dto.getCategoriaInsumoId()));

        Insumo insumo = new Insumo();
        insumo.setNombre(dto.getNombre());
        insumo.setCantidad(dto.getCantidad());
        insumo.setUnidad(dto.getUnidad() != null ? dto.getUnidad() : "pza");
        insumo.setPrecioUnitario(dto.getPrecioUnitario());
        insumo.setCategoriaInsumo(categoria);
        if (dto.getFechaIngreso() != null) {
            insumo.setFechaIngreso(LocalDate.parse(dto.getFechaIngreso()).atStartOfDay());
        }

        return toDTO(insumoRepository.save(insumo));
    }

    @Transactional
    public InsumoDTO update(Long id, InsumoDTO dto) {
        Insumo insumo = insumoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado: " + id));

        insumo.setNombre(dto.getNombre());
        insumo.setCantidad(dto.getCantidad());
        if (dto.getUnidad() != null) insumo.setUnidad(dto.getUnidad());
        insumo.setPrecioUnitario(dto.getPrecioUnitario());
        if (dto.getFechaIngreso() != null) {
            insumo.setFechaIngreso(LocalDate.parse(dto.getFechaIngreso()).atStartOfDay());
        }

        if (dto.getCategoriaInsumoId() != null) {
            CategoriaInsumo categoria = categoriaInsumoRepository.findById(dto.getCategoriaInsumoId())
                    .orElseThrow(() -> new RuntimeException("Categoria de insumo no encontrada: " + dto.getCategoriaInsumoId()));
            insumo.setCategoriaInsumo(categoria);
        }

        return toDTO(insumoRepository.save(insumo));
    }

    @Transactional
    public void delete(Long id) {
        if (!insumoRepository.existsById(id)) {
            throw new RuntimeException("Insumo no encontrado: " + id);
        }
        insumoRepository.deleteById(id);
    }

    private InsumoDTO toDTO(Insumo i) {
        InsumoDTO dto = new InsumoDTO();
        dto.setId(i.getId());
        dto.setNombre(i.getNombre());
        dto.setCantidad(i.getCantidad());
        dto.setUnidad(i.getUnidad());
        dto.setPrecioUnitario(i.getPrecioUnitario());
        dto.setFechaIngreso(i.getFechaIngreso().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dto.setCategoriaInsumoId(i.getCategoriaInsumo().getId());
        dto.setCategoriaInsumoNombre(i.getCategoriaInsumo().getNombre());
        return dto;
    }
}
