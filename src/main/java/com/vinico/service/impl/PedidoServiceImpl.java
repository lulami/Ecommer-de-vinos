package com.vinico.service.impl;

import com.vinico.dto.request.ItemPedidoRequest;
import com.vinico.dto.request.PedidoRequest;
import com.vinico.dto.response.PedidoResponse;
import com.vinico.exception.RecursoNoEncontradoException;
import com.vinico.model.*;
import com.vinico.repository.PedidoRepository;
import com.vinico.repository.UsuarioRepository;
import com.vinico.repository.VinoRepository;
import com.vinico.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinoRepository vinoRepository;

    @Override
    @Transactional
    public PedidoResponse crearPedido(PedidoRequest request, String emailCliente) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + emailCliente));

        Pedido pedido = Pedido.builder()
            .usuario(cliente)
            .estado(Pedido.EstadoPedido.PENDIENTE)
            .direccionEnvio(request.getDireccionEntrega())
            .total(BigDecimal.ZERO)
            .build();

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoRequest itemReq : request.getItems()) {
            Vino vino = vinoRepository.findById(itemReq.getVinoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vino no encontrado: " + itemReq.getVinoId()));

            if (vino.getStock() < itemReq.getCantidad()) {
                throw new IllegalStateException(
                    "Stock insuficiente para " + vino.getNombre() +
                    ". Disponible: " + vino.getStock());
            }

            ItemPedido item = ItemPedido.builder()
                .pedido(pedido)
                .vino(vino)
                .cantidad(itemReq.getCantidad())
                .precioUnitario(vino.getPrecio())
                .build();

            pedido.getItems().add(item);
            total = total.add(vino.getPrecio().multiply(BigDecimal.valueOf(itemReq.getCantidad())));

            // Descontar stock
            vino.setStock(vino.getStock() - itemReq.getCantidad());
            vinoRepository.save(vino);
        }

        pedido.setTotal(total);
        Pedido saved = pedidoRepository.save(pedido);

        log.info("Pedido {} creado para cliente {} por ${}", saved.getId(), emailCliente, total);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarMisPedidos(String emailCliente) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
            .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + emailCliente));

        return pedidoRepository.findAll().stream()
            .filter(p -> p.getUsuario().getId().equals(cliente.getId()))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoResponse obtenerPedido(Long pedidoId, String emailCliente) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + pedidoId));

        // Verificar que el pedido pertenece al cliente (o es admin)
        boolean isOwner = pedido.getUsuario().getEmail().equals(emailCliente);
        boolean isAdmin = usuarioRepository.findByEmail(emailCliente)
            .map(u -> u.getRol() == Usuario.Rol.COMERCIANTE)
            .orElse(false);

        if (!isOwner && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                "No tiene permiso para ver este pedido");
        }

        return toResponse(pedido);
    }

    @Override
    @Transactional
    public PedidoResponse cambiarEstado(Long pedidoId, String nuevoEstado, String emailAdmin) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + pedidoId));

        Pedido.EstadoPedido estado;
        try {
            estado = Pedido.EstadoPedido.valueOf(nuevoEstado.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado +
                ". Valores válidos: PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO");
        }

        pedido.setEstado(estado);

        // Si se cancela, restaurar stock
        if (estado == Pedido.EstadoPedido.CANCELADO) {
            for (ItemPedido item : pedido.getItems()) {
                Vino vino = item.getVino();
                vino.setStock(vino.getStock() + item.getCantidad());
                vinoRepository.save(vino);
            }
        }

        Pedido saved = pedidoRepository.save(pedido);
        log.info("Pedido {} cambió a estado {} por {}", pedidoId, nuevoEstado, emailAdmin);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponse> listarTodos() {
        return pedidoRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ─────────────────────────── mapper ───────────────────────────

    private PedidoResponse toResponse(Pedido pedido) {
        List<PedidoResponse.ItemPedidoResponse> itemsResp = pedido.getItems().stream()
            .map(item -> PedidoResponse.ItemPedidoResponse.builder()
                .id(item.getId())
                .vinoId(item.getVino().getId())
                .vinoNombre(item.getVino().getNombre())
                .cantidad(item.getCantidad())
                .precioUnitario(item.getPrecioUnitario())
                .subtotal(item.getSubtotal())
                .build())
            .collect(Collectors.toList());

        return PedidoResponse.builder()
            .id(pedido.getId())
            .clienteId(pedido.getUsuario().getId())
            .clienteNombre(pedido.getUsuario().getNombre())
            .estado(pedido.getEstado().name())
            .total(pedido.getTotal())
            .fechaCreacion(pedido.getFechaPedido())
            .direccionEntrega(pedido.getDireccionEnvio())
            .items(itemsResp)
            .build();
    }
}
