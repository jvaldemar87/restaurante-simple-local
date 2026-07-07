package com.restaurante.service;

import com.restaurante.dto.CategoriaDTO;
import com.restaurante.model.Categoria;
import com.restaurante.repository.CategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<CategoriaDTO> findAll() {
        return categoriaRepository.findAllByOrderByOrdenAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CategoriaDTO findById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada: " + id));
        return toDTO(categoria);
    }

    public CategoriaDTO create(CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        Integer maxOrden = categoriaRepository.findAllByOrderByOrdenAsc().stream()
                .mapToInt(Categoria::getOrden).max().orElse(0);
        categoria.setOrden(maxOrden + 1);
        return toDTO(categoriaRepository.save(categoria));
    }

    public CategoriaDTO update(Long id, CategoriaDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada: " + id));
        categoria.setNombre(dto.getNombre());
        if (dto.getOrden() != null) categoria.setOrden(dto.getOrden());
        return toDTO(categoriaRepository.save(categoria));
    }

    public void delete(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoria no encontrada: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    private CategoriaDTO toDTO(Categoria c) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setOrden(c.getOrden());
        return dto;
    }
}
