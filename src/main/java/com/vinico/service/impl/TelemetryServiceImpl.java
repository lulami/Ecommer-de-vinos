package com.vinico.service.impl;

import com.vinico.dto.request.TelemetryIngestRequest;
import com.vinico.dto.response.TelemetryResponse;
import com.vinico.model.TelemetryReading;
import com.vinico.repository.TelemetryReadingRepository;
import com.vinico.service.TelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService {

    private final TelemetryReadingRepository telemetryRepository;

    // Map de suitcaseId → lista de emitters activos
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // Umbral de temperatura para alertas
    private static final double TEMP_WARN = 18.0;
    private static final double TEMP_ALERT = 25.0;

    @Override
    public TelemetryResponse ingest(TelemetryIngestRequest request) {
        TelemetryReading reading = new TelemetryReading();
        reading.setRecordedAt(LocalDateTime.now());
        reading.setSuitcaseId(request.getSuitcaseId());
        reading.setOrderId(request.getOrderId());
        reading.setTemperatureCelsius(request.getTemperatureCelsius());
        reading.setHumidityPct(request.getHumidityPct());
        reading.setSealIntact(request.getSealIntact() != null ? request.getSealIntact() : true);
        reading.setBatteryLevel(request.getBatteryLevel());
        reading.setLatitude(request.getLatitude());
        reading.setLongitude(request.getLongitude());

        TelemetryReading saved = telemetryRepository.save(reading);

        // Calcular alerta
        String alertLevel = "OK";
        String alertMessage = "Condiciones normales";
        double tempVal = request.getTemperatureCelsius().doubleValue();

        if (!Boolean.TRUE.equals(request.getSealIntact())) {
            alertLevel = "ALERT_SEAL";
            alertMessage = "ALERTA: Precinto de valija roto";
        } else if (tempVal > TEMP_ALERT) {
            alertLevel = "ALERT_BREACH";
            alertMessage = "CRÍTICO: Temperatura " + tempVal + "°C supera límite " + TEMP_ALERT + "°C";
        } else if (tempVal > TEMP_WARN) {
            alertLevel = "WARN_TEMP";
            alertMessage = "AVISO: Temperatura " + tempVal + "°C cerca del límite recomendado";
        }

        TelemetryResponse response = TelemetryResponse.builder()
            .id(saved.getId())
            .recordedAt(saved.getRecordedAt())
            .suitcaseId(saved.getSuitcaseId())
            .orderId(saved.getOrderId())
            .temperatureCelsius(saved.getTemperatureCelsius())
            .humidityPct(saved.getHumidityPct())
            .sealIntact(saved.getSealIntact())
            .batteryLevel(saved.getBatteryLevel())
            .latitude(saved.getLatitude())
            .longitude(saved.getLongitude())
            .alertLevel(alertLevel)
            .alertMessage(alertMessage)
            .build();

        // Emitir a todos los suscriptores activos de esta maleta
        broadcastToEmitters(request.getSuitcaseId(), response);

        return response;
    }

    @Override
    public List<TelemetryResponse> getHistory(String suitcaseId, int limit) {
        return telemetryRepository
            .findBySuitcaseIdOrderByRecordedAtDesc(suitcaseId, PageRequest.of(0, limit))
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<TelemetryResponse> getThermalExcursions(double threshold) {
        return telemetryRepository
            .findByTemperatureCelsiusGreaterThan(new BigDecimal(String.valueOf(threshold)))
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public SseEmitter createEmitter(String suitcaseId) {
        // 30 minutos máximo
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        emitters.computeIfAbsent(suitcaseId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        log.info("SSE emitter registrado para maleta {}", suitcaseId);

        emitter.onCompletion(() -> {
            removeEmitter(suitcaseId, emitter);
            log.debug("SSE emitter completado para maleta {}", suitcaseId);
        });
        emitter.onTimeout(() -> {
            removeEmitter(suitcaseId, emitter);
            log.debug("SSE emitter timeout para maleta {}", suitcaseId);
        });
        emitter.onError(e -> {
            removeEmitter(suitcaseId, emitter);
            log.debug("SSE emitter error para maleta {}: {}", suitcaseId, e.getMessage());
        });

        // Enviar evento de conexión establecida
        try {
            emitter.send(SseEmitter.event()
                .name("connected")
                .data("{\"status\":\"ok\",\"suitcaseId\":\"" + suitcaseId + "\"}"));
        } catch (IOException e) {
            log.warn("Error al enviar evento de conexión SSE: {}", e.getMessage());
        }

        return emitter;
    }

    // ─────────────────────────── helpers ───────────────────────────

    private void broadcastToEmitters(String suitcaseId, TelemetryResponse payload) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(suitcaseId);
        if (list == null || list.isEmpty()) return;

        List<SseEmitter> dead = new java.util.ArrayList<>();
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                    .name("telemetry")
                    .data(payload));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        list.removeAll(dead);
    }

    private void removeEmitter(String suitcaseId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> list = emitters.get(suitcaseId);
        if (list != null) {
            list.remove(emitter);
        }
    }

    private TelemetryResponse toResponse(TelemetryReading r) {
        return TelemetryResponse.builder()
            .id(r.getId())
            .recordedAt(r.getRecordedAt())
            .suitcaseId(r.getSuitcaseId())
            .orderId(r.getOrderId())
            .temperatureCelsius(r.getTemperatureCelsius())
            .humidityPct(r.getHumidityPct())
            .sealIntact(r.getSealIntact())
            .batteryLevel(r.getBatteryLevel())
            .latitude(r.getLatitude())
            .longitude(r.getLongitude())
            .alertLevel("OK")
            .alertMessage("Histórico")
            .build();
    }
}
