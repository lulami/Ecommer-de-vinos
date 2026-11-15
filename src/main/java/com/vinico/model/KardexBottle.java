package com.vinico.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kardex_bottles",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"niche_id", "slot_index"}, name = "uq_niche_slot")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KardexBottle {

    @Id
    @Column(name = "bottle_id", length = 36)
    private String bottleId;

    @Column(name = "sku", nullable = false, length = 64)
    private String sku;

    @Column(name = "bottle_serial", nullable = false, unique = true, length = 64)
    private String bottleSerial;

    @Column(name = "vintage_year", nullable = false)
    private Integer vintageYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "niche_id", nullable = false)
    private VaultNiche vaultNiche;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private WineBottleStatus status = WineBottleStatus.IN_VAULT_AVAILABLE;

    @Column(name = "seal_number", unique = true, length = 64)
    private String sealNumber;

    @Column(name = "current_order_id", length = 32)
    private String currentOrderId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (bottleId == null) {
            bottleId = UUID.randomUUID().toString();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum WineBottleStatus {
        IN_VAULT_AVAILABLE,
        RESERVED_FOR_DISPATCH,
        IN_TRANSIT_CLIMATIZED,
        DELIVERED_NOTARIZED,
        INCIDENT_QUARANTINE_TCA,
        INCIDENT_THERMAL_EXCURSION
    }
}
