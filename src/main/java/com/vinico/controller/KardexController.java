package com.vinico.controller;

import com.vinico.dto.request.KardexHandoverRequest;
import com.vinico.dto.request.KardexReserveRequest;
import com.vinico.dto.response.KardexHandoverReceiptResponse;
import com.vinico.dto.response.KardexReservationResponse;
import com.vinico.service.KardexService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kardex")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
@RequiredArgsConstructor
public class KardexController {

    private final KardexService kardexService;

    /**
     * POST /api/kardex/reserve
     * Reserva slots físicos para un pedido.
     * Accesible por COMERCIANTE o sistema interno.
     */
    @PostMapping("/reserve")
    @PreAuthorize("hasAnyRole('COMERCIANTE', 'CLIENTE')")
    public ResponseEntity<KardexReservationResponse> reserve(
            @Valid @RequestBody KardexReserveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(kardexService.reserveNicheSlots(request));
    }

    /**
     * POST /api/kardex/egress/handover
     * Procesa el acta criptográfica de entrega.
     */
    @PostMapping("/egress/handover")
    @PreAuthorize("hasAnyRole('COMERCIANTE', 'CLIENTE')")
    public ResponseEntity<KardexHandoverReceiptResponse> handover(
            @Valid @RequestBody KardexHandoverRequest request) {
        return ResponseEntity.ok(kardexService.processHandover(request));
    }

    /**
     * POST /api/kardex/twin-bottle/dispatch
     * Despacha botella de reemplazo por incidente térmico.
     */
    @PostMapping("/twin-bottle/dispatch")
    @PreAuthorize("hasRole('COMERCIANTE')")
    public ResponseEntity<KardexReservationResponse> dispatchTwin(
            @RequestParam String incidentBottleId,
            @RequestParam String orderId) {
        return ResponseEntity.ok(kardexService.dispatchTwinBottle(incidentBottleId, orderId));
    }
}
