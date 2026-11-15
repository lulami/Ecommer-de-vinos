package com.vinico.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Reemplaza Redis para garantía de idempotencia en endpoints críticos.
 * Almacena la respuesta cacheada durante 24h para evitar doble procesamiento.
 */
@Entity
@Table(name = "idempotency_records",
       indexes = @Index(name = "idx_idempotency_key", columnList = "idempotency_key"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "operation", nullable = false, length = 64)
    private String operation;

    @Column(name = "response_body", nullable = false, columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
