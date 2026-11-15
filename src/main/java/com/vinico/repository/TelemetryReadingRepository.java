package com.vinico.repository;

import com.vinico.model.TelemetryReading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TelemetryReadingRepository extends JpaRepository<TelemetryReading, Long> {

    List<TelemetryReading> findBySuitcaseIdOrderByRecordedAtDesc(String suitcaseId, Pageable pageable);

    List<TelemetryReading> findBySuitcaseIdAndOrderIdOrderByRecordedAtDesc(String suitcaseId, String orderId);

    List<TelemetryReading> findByRecordedAtAfterAndSuitcaseId(LocalDateTime after, String suitcaseId);

    // Alertas: temperaturas sobre umbral
    List<TelemetryReading> findByTemperatureCelsiusGreaterThan(java.math.BigDecimal threshold);
}
