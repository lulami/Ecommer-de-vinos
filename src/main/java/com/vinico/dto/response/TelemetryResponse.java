package com.vinico.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TelemetryResponse {

    private Long id;
    private LocalDateTime recordedAt;
    private String suitcaseId;
    private String orderId;
    private BigDecimal temperatureCelsius;
    private BigDecimal humidityPct;
    private Boolean sealIntact;
    private Integer batteryLevel;
    private BigDecimal latitude;
    private BigDecimal longitude;

    /** Alertas generadas automáticamente al ingestar */
    private String alertLevel;   // OK, WARN_TEMP, ALERT_BREACH, ALERT_SEAL
    private String alertMessage;
}
