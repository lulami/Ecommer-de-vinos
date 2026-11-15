package com.vinico.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kardex_handover_receipts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KardexHandoverReceipt {

    @Id
    @Column(name = "receipt_id", length = 36)
    private String receiptId;

    @Column(name = "acta_notariada_number", nullable = false, unique = true, length = 32)
    private String actaNotariadaNumber;

    @Column(name = "order_id", nullable = false, length = 32)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 32)
    private HandoverChannelType channelType;

    @Column(name = "sommelier_id", nullable = false, length = 32)
    private String sommelierId;

    // Nombre con el typo intencional para coincidir con DTOs de request/response
    @Column(name = "sommilier_name", nullable = false, length = 128)
    private String sommilierName;

    @Column(name = "signatory_dni", nullable = false, length = 16)
    private String signatoryDni;

    @Column(name = "signatory_name", nullable = false, length = 128)
    private String signatoryName;

    @Column(name = "pin_validated", nullable = false)
    private Boolean pinValidated;

    @Column(name = "temperature_celsius", nullable = false, precision = 4, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "humidity_pct", nullable = false, precision = 4, scale = 2)
    private BigDecimal humidityPct;

    @Column(name = "suitcase_id", nullable = false, length = 32)
    private String suitcaseId;

    @Column(name = "vector_stroke_data", nullable = false, columnDefinition = "TEXT")
    private String vectorStrokeData;

    @Column(name = "signature_sha256", nullable = false, length = 64)
    private String signatureSha256;

    @Column(name = "blockchain_tx", length = 66)
    private String blockchainTx;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (receiptId == null) {
            receiptId = UUID.randomUUID().toString();
        }
    }

    public enum HandoverChannelType {
        VAULT_COUNTER,
        MOBILE_COURIER_DOORSTEP
    }
}
