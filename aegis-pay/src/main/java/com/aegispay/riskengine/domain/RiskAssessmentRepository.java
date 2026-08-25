package com.aegispay.riskengine.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessmentEntity, UUID> {

    List<RiskAssessmentEntity> findTop20ByUserIdOrderByEvaluatedAtDesc(String userId);

    List<RiskAssessmentEntity> findByUserIdAndRiskScoreGreaterThanEqual(String userId, int minScore);
}
