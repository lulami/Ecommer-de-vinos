package com.vinico.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class KardexHandoverReceiptResponse {

    private String receiptId;
    private String actaNotariadaNumber;
    private String orderId;
    private String channelType;
    private String sommelierId;
    private String sommilierName;
    private String signatoryDni;
    private String signatoryName;
    private Boolean pinValidated;
    private BigDecimal temperatureCelsius;
    private BigDecimal humidityPct;
    private String suitcaseId;
    private String signatureSha256;
    private String blockchainTx;
    private LocalDateTime createdAt;
}
