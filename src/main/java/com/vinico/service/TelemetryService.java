package com.vinico.service;

import com.vinico.dto.request.TelemetryIngestRequest;
import com.vinico.dto.response.TelemetryResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface TelemetryService {

    /**
     * Ingesta una lectura de telemetría IoT.
     * Persiste y emite evento SSE a suscriptores activos.
     */
    TelemetryResponse ingest(TelemetryIngestRequest request);

    /**
     * Retorna historial de lecturas para una maleta/valija.
     */
    List<TelemetryResponse> getHistory(String suitcaseId, int limit);

    /**
     * Retorna lecturas de excursión térmica (> umbral).
     */
    List<TelemetryResponse> getThermalExcursions(double threshold);

    /**
     * Crea un SseEmitter y lo registra para recibir eventos en tiempo real.
     * El emitter vive durante 30 minutos máximo.
     */
    SseEmitter createEmitter(String suitcaseId);
}
