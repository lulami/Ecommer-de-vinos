package com.vinico.service;

import com.vinico.dto.request.KardexHandoverRequest;
import com.vinico.dto.request.KardexReserveRequest;
import com.vinico.dto.response.KardexHandoverReceiptResponse;
import com.vinico.dto.response.KardexReservationResponse;

public interface KardexService {

    /**
     * Reserva slots físicos en el vault para un pedido.
     * Aplica bloqueo pesimista (selección y actualización en transacción).
     */
    KardexReservationResponse reserveNicheSlots(KardexReserveRequest request);

    /**
     * Procesa el acta criptográfica de entrega (handover).
     * Valida QR o PIN, genera SHA-256 de firma y registra en blockchain mock.
     */
    KardexHandoverReceiptResponse processHandover(KardexHandoverRequest request);

    /**
     * Despacha un "twin bottle" como reemplazo térmico de emergencia.
     * Genera voucher de compensación y actualiza estado de la botella incidente.
     */
    KardexReservationResponse dispatchTwinBottle(String incidentBottleId, String orderId);
}
