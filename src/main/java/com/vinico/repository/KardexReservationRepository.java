package com.vinico.repository;

import com.vinico.model.KardexReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KardexReservationRepository extends JpaRepository<KardexReservation, String> {

    Optional<KardexReservation> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);
}
