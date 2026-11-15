package com.vinico.controller;

import com.vinico.dto.request.TelemetryIngestRequest;
import com.vinico.dto.response.TelemetryResponse;
import com.vinico.service.TelemetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/telemetry")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService telemetryService;

    /**
     * GET /api/telemetry/stream/{suitcaseId}
     * Abre un Server-Sent Events stream para la maleta/valija climatizada.
     * Acceso público para dispositivos IoT.
     */
    @GetMapping(value = "/stream/{suitcaseId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTelemetry(@PathVariable String suitcaseId) {
        return telemetryService.createEmitter(suitcaseId);
    }

    /**
     * POST /api/telemetry/ingest
     * Ingesta una lectura de sensor IoT.
     * Acceso público (el dispositivo IoT no tiene JWT).
     */
    @PostMapping("/ingest")
    public ResponseEntity<TelemetryResponse> ingest(
            @Valid @RequestBody TelemetryIngestRequest request) {
        return ResponseEntity.ok(telemetryService.ingest(request));
    }

    /**
     * GET /api/telemetry/history/{suitcaseId}
     * Retorna historial de lecturas (últimas N).
     */
    @GetMapping("/history/{suitcaseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TelemetryResponse>> history(
            @PathVariable String suitcaseId,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(telemetryService.getHistory(suitcaseId, limit));
    }

    /**
     * GET /api/telemetry/excursions
     * Retorna lecturas con excursión térmica (admin).
     */
    @GetMapping("/excursions")
    @PreAuthorize("hasRole('COMERCIANTE')")
    public ResponseEntity<List<TelemetryResponse>> excursions(
            @RequestParam(defaultValue = "25.0") double threshold) {
        return ResponseEntity.ok(telemetryService.getThermalExcursions(threshold));
    }
}
