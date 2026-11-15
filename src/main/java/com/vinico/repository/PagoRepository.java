package com.vinico.repository;

import com.vinico.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    Optional<Pago> findByPedidoId(Long pedidoId);

    Optional<Pago> findByReferenciaMercadoPago(String referencia);

    default Optional<Pago> findByTransaccionId(String id) {
        return findByReferenciaMercadoPago(id);
    }
}
