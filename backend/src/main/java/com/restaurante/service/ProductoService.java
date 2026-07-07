package com.restaurante.service;

import com.restaurante.dto.ProductoDTO;
import com.restaurante.model.Categoria;
import com.restaurante.model.Producto;
import com.restaurante.repository.CategoriaRepository;
import com.restaurante.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<ProductoDTO> findAll() {
        return productoRepository.findByDisponibleTrueOrderByNombreAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductoDTO> findByCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndDisponibleTrueOrderByNombreAsc(categoriaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductoDTO> findByCategoria(Long categoriaId, boolean incluirInactivos) {
        if (incluirInactivos) {
            return productoRepository.findByCategoriaIdOrderByNombreAsc(categoriaId).stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        return findByCategoria(categoriaId);
    }

    public ProductoDTO findById(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        return toDTO(producto);
    }

    @Transactional
    public ProductoDTO create(ProductoDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria no encontrada: " + dto.getCategoriaId()));

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setCategoria(categoria);
        producto.setDisponible(dto.getDisponible() != null ? dto.getDisponible() : true);

        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public ProductoDTO update(Long id, ProductoDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));

        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());

        if (dto.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Categoria no encontrada: " + dto.getCategoriaId()));
            producto.setCategoria(categoria);
        }

        if (dto.getDisponible() != null) producto.setDisponible(dto.getDisponible());

        return toDTO(productoRepository.save(producto));
    }

    @Transactional
    public void delete(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        producto.setDisponible(false);
        productoRepository.save(producto);
    }

    @Transactional
    public ProductoDTO restore(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        producto.setDisponible(true);
        return toDTO(productoRepository.save(producto));
    }

    private ProductoDTO toDTO(Producto p) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setPrecio(p.getPrecio());
        dto.setCategoriaId(p.getCategoria().getId());
        dto.setCategoriaNombre(p.getCategoria().getNombre());
        dto.setDisponible(p.getDisponible());
        return dto;
    }
}
