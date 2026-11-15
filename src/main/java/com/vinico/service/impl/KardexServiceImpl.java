package com.vinico.service.impl;

import com.vinico.dto.request.KardexHandoverRequest;
import com.vinico.dto.request.KardexReserveRequest;
import com.vinico.dto.response.KardexHandoverReceiptResponse;
import com.vinico.dto.response.KardexReservationResponse;
import com.vinico.exception.RecursoNoEncontradoException;
import com.vinico.model.*;
import com.vinico.model.KardexBottle.WineBottleStatus;
import com.vinico.model.KardexHandoverReceipt.HandoverChannelType;
import com.vinico.repository.*;
import com.vinico.security.CryptoUtil;
import com.vinico.service.KardexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KardexServiceImpl implements KardexService {

    private final KardexBottleRepository bottleRepository;
    private final KardexReservationRepository reservationRepository;
    private final KardexReservationItemRepository reservationItemRepository;
    private final KardexHandoverReceiptRepository receiptRepository;
    private final CustomerVoucherRepository voucherRepository;
    private final CryptoUtil cryptoUtil;

    @Override
    @Transactional
    public KardexReservationResponse reserveNicheSlots(KardexReserveRequest request) {
        // Verificar si ya existe una reserva para este orden (idempotencia)
        if (reservationRepository.existsByOrderId(request.getOrderId())) {
            KardexReservation existing = reservationRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Reserva no encontrada para orderId: " + request.getOrderId()));
            return buildReservationResponse(existing);
        }

        // Crear la reserva principal
        String pin = String.valueOf(cryptoUtil.generatePin());
        String qrSecret = cryptoUtil.randomHex(16);
        String qrHash = cryptoUtil.sha256Hex(qrSecret + request.getOrderId());

        KardexReservation reservation = new KardexReservation();
        reservation.setReservationId(UUID.randomUUID().toString());
        reservation.setOrderId(request.getOrderId());
        reservation.setClientId(request.getClientId());
        reservation.setQrSecretHash(qrHash);
        reservation.setPinFallback(pin.substring(0, 4));
        reservation.setDispatchMode(request.getDispatchMode());
        reservation.setStatus("PENDING_HANDOVER");
        reservation.setExpiresAt(LocalDateTime.now().plusHours(72));

        KardexReservation savedReservation = reservationRepository.save(reservation);

        // Reservar botellas para cada ítem solicitado
        List<KardexReservationItem> items = new ArrayList<>();
        for (KardexReserveRequest.ReserveItem reqItem : request.getItems()) {
            List<KardexBottle> available = bottleRepository
                .findAvailableBySkuAndVintage(reqItem.getSku(), reqItem.getVintageYear(),
                    WineBottleStatus.IN_VAULT_AVAILABLE);

            if (available.size() < reqItem.getQuantity()) {
                throw new IllegalStateException(
                    "Stock insuficiente para SKU " + reqItem.getSku() +
                    " vintage " + reqItem.getVintageYear() +
                    ". Disponibles: " + available.size() + ", solicitados: " + reqItem.getQuantity());
            }

            for (int i = 0; i < reqItem.getQuantity(); i++) {
                KardexBottle bottle = available.get(i);
                bottle.setStatus(WineBottleStatus.RESERVED_FOR_DISPATCH);
                bottle.setCurrentOrderId(request.getOrderId());
                bottleRepository.save(bottle);

                KardexReservationItem item = new KardexReservationItem();
                item.setId(UUID.randomUUID().toString());
                item.setReservation(savedReservation);
                item.setBottle(bottle);
                item.setAllocatedAt(LocalDateTime.now());
                items.add(reservationItemRepository.save(item));
            }
        }

        log.info("Reserva {} creada para orden {} con {} botellas",
            savedReservation.getReservationId(), request.getOrderId(), items.size());

        return buildReservationResponse(savedReservation, items, qrSecret, pin);
    }

    @Override
    @Transactional
    public KardexHandoverReceiptResponse processHandover(KardexHandoverRequest request) {
        KardexReservation reservation = reservationRepository.findByOrderId(request.getOrderId())
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "Reserva no encontrada para orderId: " + request.getOrderId()));

        // Validar QR o PIN
        boolean pinValidated = false;
        if (request.getQrToken() != null) {
            String expectedHash = cryptoUtil.sha256Hex(request.getQrToken() + request.getOrderId());
            pinValidated = cryptoUtil.timingSafeEquals(expectedHash, reservation.getQrSecretHash());
        } else if (request.getPin() != null) {
            pinValidated = cryptoUtil.timingSafeEquals(request.getPin(), reservation.getPinFallback());
        }

        if (!pinValidated) {
            throw new IllegalArgumentException("QR token o PIN inválido para la reserva");
        }

        // Construir payload de firma
        String signaturePayload = reservation.getReservationId() + "|" +
            request.getOrderId() + "|" +
            request.getSignatoryDni() + "|" +
            System.currentTimeMillis();

        String vectorData = request.getVectorStrokeData() != null
            ? request.getVectorStrokeData() : "{}";
        String signatureSha256 = cryptoUtil.sha256Hex(signaturePayload + vectorData);

        // Mock blockchain tx
        String blockchainTx = cryptoUtil.mockPolygonTx();

        // Generar número de acta
        String actaNumber = "ACTA-" + System.currentTimeMillis() + "-" +
            cryptoUtil.randomHex(4).toUpperCase();

        // Crear recibo de handover
        KardexHandoverReceipt receipt = new KardexHandoverReceipt();
        receipt.setReceiptId(UUID.randomUUID().toString());
        receipt.setActaNotariadaNumber(actaNumber);
        receipt.setOrderId(request.getOrderId());
        receipt.setChannelType(HandoverChannelType.valueOf(request.getChannelType()));
        receipt.setSommelierId(request.getSommelierId());
        receipt.setSommilierName(request.getSommilierName());
        receipt.setSignatoryDni(request.getSignatoryDni());
        receipt.setSignatoryName(request.getSignatoryName());
        receipt.setPinValidated(pinValidated);
        receipt.setTemperatureCelsius(request.getTemperatureCelsius());
        receipt.setHumidityPct(request.getHumidityPct());
        receipt.setSuitcaseId(request.getSuitcaseId());
        receipt.setVectorStrokeData(vectorData);
        receipt.setSignatureSha256(signatureSha256);
        receipt.setBlockchainTx(blockchainTx);

        KardexHandoverReceipt saved = receiptRepository.save(receipt);

        // Marcar botellas como entregadas
        List<KardexReservationItem> items = reservationItemRepository
            .findByReservationReservationId(reservation.getReservationId());
        for (KardexReservationItem item : items) {
            KardexBottle bottle = item.getBottle();
            bottle.setStatus(WineBottleStatus.DELIVERED_NOTARIZED);
            bottleRepository.save(bottle);
        }

        // Actualizar estado de la reserva
        reservation.setStatus("COMPLETED_NOTARIZED");
        reservationRepository.save(reservation);

        log.info("Handover completado para orden {}. Acta: {}. Blockchain: {}",
            request.getOrderId(), actaNumber, blockchainTx);

        return KardexHandoverReceiptResponse.builder()
            .receiptId(saved.getReceiptId())
            .actaNotariadaNumber(saved.getActaNotariadaNumber())
            .orderId(saved.getOrderId())
            .channelType(saved.getChannelType().name())
            .sommelierId(saved.getSommelierId())
            .sommilierName(saved.getSommilierName())
            .signatoryDni(saved.getSignatoryDni())
            .signatoryName(saved.getSignatoryName())
            .pinValidated(saved.getPinValidated())
            .temperatureCelsius(saved.getTemperatureCelsius())
            .humidityPct(saved.getHumidityPct())
            .suitcaseId(saved.getSuitcaseId())
            .signatureSha256(saved.getSignatureSha256())
            .blockchainTx(saved.getBlockchainTx())
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Override
    @Transactional
    public KardexReservationResponse dispatchTwinBottle(String incidentBottleId, String orderId) {
        KardexBottle incidentBottle = bottleRepository.findById(incidentBottleId)
            .orElseThrow(() -> new RecursoNoEncontradoException(
                "Botella incidente no encontrada: " + incidentBottleId));

        // Marcar botella incidente como excursión térmica
        incidentBottle.setStatus(WineBottleStatus.INCIDENT_THERMAL_EXCURSION);
        bottleRepository.save(incidentBottle);

        // Buscar botella twin del mismo SKU y vintage
        List<KardexBottle> available = bottleRepository
            .findAvailableBySkuAndVintage(incidentBottle.getSku(),
                incidentBottle.getVintageYear(), WineBottleStatus.IN_VAULT_AVAILABLE);

        if (available.isEmpty()) {
            // No hay twin disponible — emitir voucher de compensación
            CustomerVoucher voucher = new CustomerVoucher();
            voucher.setOrderId(orderId);
            voucher.setVoucherCode("VOUCHER-" + cryptoUtil.randomHex(8).toUpperCase());
            voucher.setAmountArs(new java.math.BigDecimal("5000.00"));
            voucher.setReason("Incidencia térmica — sin stock twin disponible");
            voucher.setIssuedBy("KARDEX_SYSTEM");
            voucher.setStatus("ACTIVE_IN_WALLET");
            voucherRepository.save(voucher);

            log.warn("No hay twin disponible para botella {}. Voucher emitido.", incidentBottleId);
            throw new IllegalStateException(
                "No hay twin disponible. Se emitió voucher de compensación para orden " + orderId);
        }

        // Crear reserva de reemplazo
        KardexReserveRequest twinRequest = new KardexReserveRequest();
        twinRequest.setOrderId("TWIN-" + orderId);
        twinRequest.setClientId("SYSTEM_DISPATCH");
        twinRequest.setDispatchMode("IMMEDIATE_REPLACEMENT");

        KardexReserveRequest.ReserveItem twinItem = new KardexReserveRequest.ReserveItem();
        twinItem.setSku(incidentBottle.getSku());
        twinItem.setVintageYear(incidentBottle.getVintageYear());
        twinItem.setQuantity(1);
        twinRequest.setItems(List.of(twinItem));

        log.info("Twin bottle dispatch para botella incidente {}. Orden: {}", incidentBottleId, orderId);
        return reserveNicheSlots(twinRequest);
    }

    // ─────────────────────────── helpers ───────────────────────────

    private KardexReservationResponse buildReservationResponse(KardexReservation reservation) {
        List<KardexReservationItem> items = reservationItemRepository
            .findByReservationReservationId(reservation.getReservationId());
        return buildReservationResponse(reservation, items, null, null);
    }

    private KardexReservationResponse buildReservationResponse(
            KardexReservation reservation,
            List<KardexReservationItem> items,
            String qrSecret,
            String pin) {

        List<KardexReservationResponse.ReservedBottleInfo> bottleInfos = items.stream()
            .map(item -> {
                KardexBottle b = item.getBottle();
                return KardexReservationResponse.ReservedBottleInfo.builder()
                    .bottleId(b.getBottleId())
                    .sku(b.getSku())
                    .vintageYear(b.getVintageYear())
                    .bottleSerial(b.getBottleSerial())
                    .nicheId(b.getVaultNiche() != null ? b.getVaultNiche().getNicheId() : null)
                    .slotIndex(b.getSlotIndex())
                    .build();
            })
            .collect(Collectors.toList());

        // Payload QR = base64(orderId:qrSecret)
        String qrPayload = qrSecret != null
            ? Base64.getEncoder().encodeToString((reservation.getOrderId() + ":" + qrSecret).getBytes())
            : null;

        return KardexReservationResponse.builder()
            .reservationId(reservation.getReservationId())
            .orderId(reservation.getOrderId())
            .clientId(reservation.getClientId())
            .dispatchMode(reservation.getDispatchMode())
            .status(reservation.getStatus())
            .expiresAt(reservation.getExpiresAt())
            .qrPayload(qrPayload)
            .pinFallback(pin != null ? pin.substring(0, 4) : reservation.getPinFallback())
            .bottles(bottleInfos)
            .build();
    }
}
