package com.timecapsule.api.audit.web;

import com.timecapsule.api.common.domain.CapsuleEventType;
import java.time.Instant;
import java.util.UUID;

public record CapsuleEventResponse(
		UUID id,
		UUID capsuleId,
		CapsuleEventType eventType,
		UUID actorId,
		String metadata,
		Instant createdAt) {
}
