package com.vinico.service;

import com.vinico.dto.request.PedidoRequest;
import com.vinico.dto.response.PedidoResponse;
import com.vinico.model.Pedido;

import java.util.List;

public interface PedidoService {

    PedidoResponse crearPedido(PedidoRequest request, String emailCliente);

    List<PedidoResponse> listarMisPedidos(String emailCliente);

    PedidoResponse obtenerPedido(Long pedidoId, String emailCliente);

    PedidoResponse cambiarEstado(Long pedidoId, String nuevoEstado, String emailAdmin);

    List<PedidoResponse> listarTodos();
}
