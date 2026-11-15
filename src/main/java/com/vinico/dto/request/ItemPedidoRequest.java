package com.vinico.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemPedidoRequest {

    @NotNull
    private Long vinoId;

    @NotNull
    @Min(1)
    private Integer cantidad;
}
