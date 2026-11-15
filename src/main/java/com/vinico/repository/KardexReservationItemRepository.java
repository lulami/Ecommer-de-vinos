package com.vinico.repository;

import com.vinico.model.KardexReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KardexReservationItemRepository extends JpaRepository<KardexReservationItem, String> {

    List<KardexReservationItem> findByReservationReservationId(String reservationId);
}
