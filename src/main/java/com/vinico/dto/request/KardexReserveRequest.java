package com.vinico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class KardexReserveRequest {

    @NotBlank
    private String orderId;

    @NotBlank
    private String clientId;

    /** Lista de ítems a reservar: cada uno con sku, vintageYear y cantidad */
    @NotNull
    private List<ReserveItem> items;

    /** VAULT_PICKUP o HOME_DELIVERY */
    private String dispatchMode = "VAULT_PICKUP";

    @Data
    public static class ReserveItem {
        @NotBlank
        private String sku;

        @NotNull
        private Integer vintageYear;

        @NotNull
        @Positive
        private Integer quantity;
    }
}
