package com.vinico.repository;

import com.vinico.model.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKey(String key);

    @Modifying
    @Transactional
    @Query("DELETE FROM IdempotencyRecord r WHERE r.expiresAt < :now")
    void deleteExpired(LocalDateTime now);
}
