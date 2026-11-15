package com.vinico.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class KardexReservationResponse {

    private String reservationId;
    private String orderId;
    private String clientId;
    private String dispatchMode;
    private String status;
    private LocalDateTime expiresAt;
    private String qrPayload;    // base64 del QR para escanear
    private String pinFallback;  // PIN de 4 dígitos

    private List<ReservedBottleInfo> bottles;

    @Data
    @Builder
    public static class ReservedBottleInfo {
        private String bottleId;
        private String sku;
        private Integer vintageYear;
        private String bottleSerial;
        private String nicheId;
        private Integer slotIndex;
    }
}
