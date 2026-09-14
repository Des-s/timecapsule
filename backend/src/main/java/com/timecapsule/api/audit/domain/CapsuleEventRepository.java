package com.timecapsule.api.audit.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapsuleEventRepository extends JpaRepository<CapsuleEventEntity, UUID> {

	List<CapsuleEventEntity> findByCapsuleIdOrderByCreatedAtAsc(UUID capsuleId);
}
