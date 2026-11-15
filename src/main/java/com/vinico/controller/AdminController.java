package com.vinico.controller;

import com.vinico.dto.response.AdminMetricasResponse;
import com.vinico.dto.response.PedidoResponse;
import com.vinico.model.KardexBottle.WineBottleStatus;
import com.vinico.model.Pedido;
import com.vinico.repository.*;
import com.vinico.service.PedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:3000"})
@RequiredArgsConstructor
@PreAuthorize("hasRole('COMERCIANTE')")
public class AdminController {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VinoRepository vinoRepository;
    private final KardexBottleRepository bottleRepository;
    private final VaultNicheRepository vaultNicheRepository;
    private final PedidoService pedidoService;

    /**
     * GET /api/admin/metricas
     * Panel de métricas generales del negocio.
     */
    @GetMapping("/metricas")
    public ResponseEntity<AdminMetricasResponse> metricas() {
        List<Pedido> todos = pedidoRepository.findAll();

        Map<String, Long> porEstado = todos.stream()
            .collect(Collectors.groupingBy(
                p -> p.getEstado().name(),
                Collectors.counting()
            ));

        BigDecimal ventasTotales = todos.stream()
            .filter(p -> p.getEstado() != Pedido.EstadoPedido.CANCELADO)
            .map(Pedido::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        BigDecimal ventasMes = todos.stream()
            .filter(p -> p.getEstado() != Pedido.EstadoPedido.CANCELADO)
            .filter(p -> p.getFechaPedido() != null && p.getFechaPedido().isAfter(inicioMes))
            .map(Pedido::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Métricas Kardex
        long disponibles = bottleRepository.findByStatus(WineBottleStatus.IN_VAULT_AVAILABLE).size();
        long reservadas = bottleRepository.findByStatus(WineBottleStatus.RESERVED_FOR_DISPATCH).size();
        long entregadas = bottleRepository.findByStatus(WineBottleStatus.DELIVERED_NOTARIZED).size();
        long incidencia = bottleRepository.findByStatus(WineBottleStatus.INCIDENT_THERMAL_EXCURSION).size() +
                         bottleRepository.findByStatus(WineBottleStatus.INCIDENT_QUARANTINE_TCA).size();

        return ResponseEntity.ok(AdminMetricasResponse.builder()
            .totalPedidos((long) todos.size())
            .pedidosPendientes(porEstado.getOrDefault("PENDIENTE", 0L))
            .pedidosEntregados(porEstado.getOrDefault("ENTREGADO", 0L))
            .totalClientes(usuarioRepository.count())
            .totalProductos(vinoRepository.count())
            .ventasTotales(ventasTotales)
            .ventasMes(ventasMes)
            .pedidosPorEstado(porEstado)
            .botellasDisponibles(disponibles)
            .botellasReservadas(reservadas)
            .botellasEntregadas(entregadas)
            .botellasIncidencia(incidencia)
            .nichesActivos((long) vaultNicheRepository.findAll().size())
            .build());
    }

    /**
     * GET /api/admin/stock
     * Inventario de vinos con stock actual.
     */
    @GetMapping("/stock")
    public ResponseEntity<?> stock() {
        return ResponseEntity.ok(
            vinoRepository.findAll().stream()
                .map(v -> Map.of(
                    "id", v.getId(),
                    "nombre", v.getNombre(),
                    "tipo", v.getTipo().name(),
                    "stock", v.getStock(),
                    "precio", v.getPrecio()
                ))
                .collect(Collectors.toList())
        );
    }

    /**
     * GET /api/admin/ventas
     * Listado completo de pedidos para gestión.
     */
    @GetMapping("/ventas")
    public ResponseEntity<List<PedidoResponse>> ventas() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }
}
