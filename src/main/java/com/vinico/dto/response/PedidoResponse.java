package com.vinico.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PedidoResponse {

    private Long id;
    private Long clienteId;
    private String clienteNombre;
    private String estado;
    private BigDecimal total;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private String direccionEntrega;
    private String notas;
    private List<ItemPedidoResponse> items;

    @Data
    @Builder
    public static class ItemPedidoResponse {
        private Long id;
        private Long vinoId;
        private String vinoNombre;
        private Integer cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
