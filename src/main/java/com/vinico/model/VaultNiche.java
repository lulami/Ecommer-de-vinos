package com.vinico.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vault_niches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaultNiche {

    @Id
    @Column(name = "niche_id", length = 16)
    private String nicheId;

    @Column(name = "zone_sector", nullable = false, length = 32)
    private String zoneSector;

    @Column(name = "total_slots", nullable = false)
    private Integer totalSlots = 18;

    @Column(name = "target_temp_min", nullable = false, precision = 4, scale = 2)
    private BigDecimal targetTempMin = new BigDecimal("14.0");

    @Column(name = "target_temp_max", nullable = false, precision = 4, scale = 2)
    private BigDecimal targetTempMax = new BigDecimal("16.0");

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
