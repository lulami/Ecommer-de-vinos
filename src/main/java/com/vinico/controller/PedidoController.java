package com.vinico.controller;

import com.vinico.dto.request.PedidoRequest;
import com.vinico.dto.response.PedidoResponse;
import com.vinico.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * POST /api/pedidos
     * Crea un nuevo pedido para el cliente autenticado.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoResponse> crear(
            @Valid @RequestBody PedidoRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(pedidoService.crearPedido(request, user.getUsername()));
    }

    /**
     * GET /api/pedidos/mis-pedidos
     * Lista los pedidos del cliente autenticado.
     */
    @GetMapping("/mis-pedidos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PedidoResponse>> misPedidos(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pedidoService.listarMisPedidos(user.getUsername()));
    }

    /**
     * GET /api/pedidos/{id}
     * Detalle de un pedido (propio o admin).
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoResponse> detalle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pedidoService.obtenerPedido(id, user.getUsername()));
    }

    /**
     * PATCH /api/pedidos/{id}/estado
     * Cambia el estado de un pedido (solo COMERCIANTE).
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('COMERCIANTE')")
    public ResponseEntity<PedidoResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado, user.getUsername()));
    }
}
