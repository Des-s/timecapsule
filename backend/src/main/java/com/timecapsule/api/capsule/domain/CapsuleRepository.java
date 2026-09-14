package com.timecapsule.api.capsule.domain;

import com.timecapsule.api.common.domain.CapsuleStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CapsuleRepository extends JpaRepository<CapsuleEntity, UUID> {

	List<CapsuleEntity> findByCreatorIdOrRecipientIdOrderByCreatedAtDesc(UUID creatorId, UUID recipientId);

	@Query("""
			SELECT c FROM CapsuleEntity c
			WHERE c.status = :status
			AND (c.nextEvaluationAt IS NULL OR c.nextEvaluationAt <= :now)
			""")
	List<CapsuleEntity> findDueForEvaluation(
			@Param("status") CapsuleStatus status,
			@Param("now") Instant now);
}
