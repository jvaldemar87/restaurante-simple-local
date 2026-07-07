package com.restaurante.service;

import com.restaurante.dto.CategoriaPagoDTO;
import com.restaurante.model.CategoriaPago;
import com.restaurante.repository.CategoriaPagoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoriaPagoService {

    private final CategoriaPagoRepository repository;

    public CategoriaPagoService(CategoriaPagoRepository repository) {
        this.repository = repository;
    }

    public List<CategoriaPagoDTO> findAll() {
        return repository.findAll().stream()
                .map(c -> { CategoriaPagoDTO d = new CategoriaPagoDTO(); d.setId(c.getId()); d.setNombre(c.getNombre()); return d; })
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoriaPagoDTO create(CategoriaPagoDTO dto) {
        CategoriaPago c = new CategoriaPago();
        c.setNombre(dto.getNombre());
        c = repository.save(c);
        CategoriaPagoDTO r = new CategoriaPagoDTO();
        r.setId(c.getId()); r.setNombre(c.getNombre());
        return r;
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
