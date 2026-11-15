package com.vinico.repository;

import com.vinico.model.KardexHandoverReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KardexHandoverReceiptRepository extends JpaRepository<KardexHandoverReceipt, String> {

    Optional<KardexHandoverReceipt> findByOrderId(String orderId);

    List<KardexHandoverReceipt> findByActaNotariadaNumber(String actaNumber);
}
