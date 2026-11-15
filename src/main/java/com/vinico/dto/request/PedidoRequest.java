package com.vinico.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PedidoRequest {

    @NotEmpty
    @Valid
    private List<ItemPedidoRequest> items;

    /** Dirección de entrega (opcional, solo para envíos a domicilio) */
    private String direccionEntrega;

    /** Notas adicionales */
    private String notas;
}
