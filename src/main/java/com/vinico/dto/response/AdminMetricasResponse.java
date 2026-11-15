package com.vinico.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class AdminMetricasResponse {

    private Long totalPedidos;
    private Long pedidosPendientes;
    private Long pedidosEntregados;
    private Long totalClientes;
    private Long totalProductos;
    private BigDecimal ventasTotales;
    private BigDecimal ventasMes;

    /** Distribución de pedidos por estado */
    private Map<String, Long> pedidosPorEstado;

    /** Stock Kardex */
    private Long botellasDisponibles;
    private Long botellasReservadas;
    private Long botellasEntregadas;
    private Long botellasIncidencia;
    private Long nichesActivos;
}
