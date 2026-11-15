package com.vinico.controller;

import com.vinico.exception.RecursoNoEncontradoException;
import com.vinico.model.Pago;
import com.vinico.model.Pedido;
import com.vinico.repository.PagoRepository;
import com.vinico.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
@RequiredArgsConstructor
public class PagoController {

    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;

    /**
     * POST /api/pagos/iniciar
     * Stub de integración con MercadoPago.
     * En producción generaría preference_id real; aquí simula el flujo.
     */
    @PostMapping("/iniciar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> iniciarPago(
            @RequestParam Long pedidoId,
            @AuthenticationPrincipal UserDetails user) {

        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + pedidoId));

        // Verificar que el pedido es del cliente autenticado
        if (!pedido.getUsuario().getEmail().equals(user.getUsername())) {
            return ResponseEntity.status(403)
                .body(Map.of("error", "No tiene permiso para pagar este pedido"));
        }

        // ── MercadoPago stub ─────────────────────────────────────────
        // En producción:
        //   PreferenceRequest preferenceReq = PreferenceRequest.builder()
        //       .items(List.of(PreferenceItemRequest.builder()
        //           .title("Pedido Vínico #" + pedidoId)
        //           .unitPrice(pedido.getTotal().floatValue())
        //           .quantity(1)
        //           .build()))
        //       .build();
        //   PreferenceResponse preference = mpClient.preferences().create(preferenceReq);
        //   return preference.getInitPoint();
        // ────────────────────────────────────────────────────────────

        String mockPreferenceId = "MP-PREF-" +
            UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        // Registrar pago en estado PENDIENTE
        Pago pago = Pago.builder()
            .pedido(pedido)
            .monto(pedido.getTotal())
            .metodoPago("MERCADO_PAGO")
            .estado(Pago.EstadoPago.PENDIENTE)
            .referenciaMercadoPago(mockPreferenceId)
            .urlPago("https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=" + mockPreferenceId)
            .fechaPago(LocalDateTime.now())
            .build();
        pagoRepository.save(pago);

        return ResponseEntity.ok(Map.of(
            "pedidoId", pedidoId,
            "monto", pedido.getTotal(),
            "preferenceId", mockPreferenceId,
            "initPoint", "https://www.mercadopago.com.ar/checkout/v1/redirect?pref_id=" + mockPreferenceId,
            "sandboxInitPoint", "https://sandbox.mercadopago.com.ar/checkout/v1/redirect?pref_id=" + mockPreferenceId,
            "estado", "PENDIENTE",
            "nota", "Integración MercadoPago stub — reemplazar con SDK real en producción"
        ));
    }

    /**
     * POST /api/pagos/webhook
     * Webhook de MercadoPago para confirmación de pago.
     * En producción validaría firma HMAC-SHA256 del header x-signature.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> webhook(
            @RequestHeader(value = "x-signature", required = false) String signature,
            @RequestBody Map<String, Object> payload) {

        // En producción: validar HMAC-SHA256
        // String expectedSig = cryptoUtil.hmacSha256(MP_WEBHOOK_SECRET, rawBody);
        // if (!cryptoUtil.timingSafeEquals(expectedSig, signature)) return 401;

        String type = (String) payload.getOrDefault("type", "");

        if ("payment".equals(type) && payload.containsKey("data")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String dataId = (String) data.getOrDefault("id", "");

            if (!dataId.isEmpty()) {
                pagoRepository.findByReferenciaMercadoPago(dataId).ifPresent(pago -> {
                    pago.setEstado(Pago.EstadoPago.APROBADO);
                    pagoRepository.save(pago);

                    Pedido pedido = pago.getPedido();
                    pedido.setEstado(Pedido.EstadoPedido.CONFIRMADO);
                    pedidoRepository.save(pedido);
                });
            }
        }

        return ResponseEntity.ok(Map.of("status", "received"));
    }
}
