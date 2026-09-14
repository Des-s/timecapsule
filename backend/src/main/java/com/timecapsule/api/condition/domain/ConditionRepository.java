package com.timecapsule.api.condition.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConditionRepository extends JpaRepository<ConditionEntity, UUID> {

	List<ConditionEntity> findByCapsuleIdOrderByCreatedAtAsc(UUID capsuleId);

	@Modifying
	@Query("DELETE FROM ConditionEntity c WHERE c.capsuleId = :capsuleId AND c.id = :conditionId")
	int deleteByCapsuleIdAndId(@Param("capsuleId") UUID capsuleId, @Param("conditionId") UUID conditionId);
}
