package com.vinico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TelemetryIngestRequest {

    @NotBlank
    private String suitcaseId;

    private String orderId;

    @NotNull
    private BigDecimal temperatureCelsius;

    @NotNull
    private BigDecimal humidityPct;

    private Boolean sealIntact = true;

    /** Nivel de batería (0-100) */
    private Integer batteryLevel;

    private BigDecimal latitude;
    private BigDecimal longitude;
}
