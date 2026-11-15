package com.vinico.service;

import com.vinico.dto.request.VinoRequest;
import com.vinico.dto.response.VinoResponse;
import com.vinico.model.Vino;

import java.util.List;

public interface VinoService {

    List<VinoResponse> listarTodos();

    List<VinoResponse> listarPorTipo(Vino.TipoVino tipo);

    VinoResponse buscarPorId(Long id);

    VinoResponse crear(VinoRequest request);

    VinoResponse actualizar(Long id, VinoRequest request);

    void actualizarStock(Long id, Integer nuevoStock);

    void desactivar(Long id);
}
