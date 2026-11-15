package com.vinico.repository;

import com.vinico.model.CustomerVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, Long> {

    List<CustomerVoucher> findByOrderId(String orderId);

    List<CustomerVoucher> findByStatus(String status);
}
