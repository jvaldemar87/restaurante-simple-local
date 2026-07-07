package com.restaurante.service;

import com.restaurante.dto.CategoriaInsumoDTO;
import com.restaurante.model.CategoriaInsumo;
import com.restaurante.repository.CategoriaInsumoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoriaInsumoService {

    private final CategoriaInsumoRepository repository;

    public CategoriaInsumoService(CategoriaInsumoRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaInsumoDTO> findAll() {
        return repository.findAll().stream()
                .map(c -> { CategoriaInsumoDTO d = new CategoriaInsumoDTO(); d.setId(c.getId()); d.setNombre(c.getNombre()); return d; })
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaInsumoDTO create(CategoriaInsumoDTO dto) {
        CategoriaInsumo c = new CategoriaInsumo();
        c.setNombre(dto.getNombre());
        c = repository.save(c);
        CategoriaInsumoDTO r = new CategoriaInsumoDTO();
        r.setId(c.getId()); r.setNombre(c.getNombre());
        return r;
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
