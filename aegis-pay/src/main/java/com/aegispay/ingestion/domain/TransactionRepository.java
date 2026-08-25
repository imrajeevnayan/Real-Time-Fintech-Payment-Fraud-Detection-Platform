package com.aegispay.ingestion.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByUserIdAndIpAddress(String userId, String ipAddress);

    List<Transaction> findByUserIdOrderByOccurredAtDesc(String userId, Pageable pageable);
}
