package com.vinico.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kardex_reservation_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KardexReservationItem {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private KardexReservation reservation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bottle_id", nullable = false)
    private KardexBottle bottle;

    @Column(name = "allocated_at", nullable = false, updatable = false)
    private LocalDateTime allocatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
