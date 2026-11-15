package com.vinico.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kardex_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KardexReservation {

    @Id
    @Column(name = "reservation_id", length = 36)
    private String reservationId;

    @Column(name = "order_id", nullable = false, unique = true, length = 32)
    private String orderId;

    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;

    @Column(name = "qr_secret_hash", nullable = false, length = 64)
    private String qrSecretHash;

    @Column(name = "pin_fallback", nullable = false, length = 4)
    private String pinFallback;

    @Column(name = "dispatch_mode", nullable = false, length = 32)
    private String dispatchMode;

    @Column(name = "status", nullable = false, length = 24)
    private String status = "PENDING_HANDOVER";

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<KardexReservationItem> items = new ArrayList<>();
}
