package com.vinico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KardexHandoverRequest {

    @NotBlank
    private String reservationId;

    @NotBlank
    private String orderId;

    /** QR token o PIN fallback */
    private String qrToken;
    private String pin;

    /** VAULT_COUNTER o MOBILE_COURIER_DOORSTEP */
    @NotBlank
    private String channelType;

    private String sommelierId;
    private String sommilierName;
    private String signatoryDni;
    private String signatoryName;

    @NotNull
    private BigDecimal temperatureCelsius;

    private BigDecimal humidityPct;
    private String suitcaseId;

    /** SVG/JSON de stroke del cliente para firma digital */
    private String vectorStrokeData;
}
