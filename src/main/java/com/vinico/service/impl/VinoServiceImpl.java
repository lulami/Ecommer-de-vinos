package com.vinico.service.impl;

import com.vinico.dto.request.VinoRequest;
import com.vinico.dto.response.VinoResponse;
import com.vinico.exception.RecursoNoEncontradoException;
import com.vinico.model.Vino;
import com.vinico.repository.VinoRepository;
import com.vinico.service.VinoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VinoServiceImpl implements VinoService {

    private final VinoRepository vinoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<VinoResponse> listarTodos() {
        return vinoRepository.findByActivoTrue()
                .stream()
                .map(VinoResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VinoResponse> listarPorTipo(Vino.TipoVino tipo) {
        return vinoRepository.findByTipoAndActivoTrue(tipo)
                .stream()
                .map(VinoResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VinoResponse buscarPorId(Long id) {
        Vino vino = vinoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vino no encontrado con id: " + id));
        return VinoResponse.fromEntity(vino);
    }

    @Override
    public VinoResponse crear(VinoRequest request) {
        Vino vino = Vino.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .tipo(Vino.TipoVino.valueOf(request.getTipo().toUpperCase()))
                .bodega(request.getBodega())
                .region(request.getRegion())
                .anio(request.getAnio())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .imagenUrl(request.getImagenUrl())
                .activo(true)
                .build();
        return VinoResponse.fromEntity(vinoRepository.save(vino));
    }

    @Override
    public VinoResponse actualizar(Long id, VinoRequest request) {
        Vino vino = vinoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vino no encontrado con id: " + id));

        vino.setNombre(request.getNombre());
        vino.setDescripcion(request.getDescripcion());
        vino.setTipo(Vino.TipoVino.valueOf(request.getTipo().toUpperCase()));
        vino.setBodega(request.getBodega());
        vino.setRegion(request.getRegion());
        vino.setAnio(request.getAnio());
        vino.setPrecio(request.getPrecio());
        vino.setImagenUrl(request.getImagenUrl());

        return VinoResponse.fromEntity(vinoRepository.save(vino));
    }

    @Override
    public void actualizarStock(Long id, Integer nuevoStock) {
        Vino vino = vinoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vino no encontrado con id: " + id));
        if (nuevoStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        vino.setStock(nuevoStock);
        vinoRepository.save(vino);
    }

    @Override
    public void desactivar(Long id) {
        Vino vino = vinoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vino no encontrado con id: " + id));
        vino.setActivo(false);
        vinoRepository.save(vino);
    }
}
