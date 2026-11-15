package com.vinico.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_vouchers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, length = 32)
    private String orderId;

    @Column(name = "voucher_code", nullable = false, unique = true, length = 32)
    private String voucherCode;

    @Column(name = "amount_ars", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountArs;

    @Column(name = "reason", length = 256)
    private String reason;

    @Column(name = "issued_by", length = 64)
    private String issuedBy;

    @Column(name = "status", length = 32)
    private String status = "ACTIVE_IN_WALLET";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
