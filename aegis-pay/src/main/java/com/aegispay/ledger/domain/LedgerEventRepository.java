package com.aegispay.ledger.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEventRepository extends JpaRepository<LedgerEvent, Long> {

    List<LedgerEvent> findByAggregateTypeAndAggregateIdOrderBySeq(
            String aggregateType, String aggregateId, Pageable pageable);
}
