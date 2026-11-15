package com.vinico.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    @Column(name = "suitcase_id", nullable = false, length = 32)
    private String suitcaseId;

    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "temperature_celsius", nullable = false, precision = 4, scale = 2)
    private BigDecimal temperatureCelsius;

    @Column(name = "humidity_pct", nullable = false, precision = 4, scale = 2)
    private BigDecimal humidityPct;

    @Column(name = "seal_intact", nullable = false)
    private Boolean sealIntact = true;

    @Column(name = "battery_level", nullable = false)
    private Integer batteryLevel = 100;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;
}
