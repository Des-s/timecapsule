package com.timecapsule.api.condition.web;

import com.timecapsule.api.common.domain.ConditionResultStatus;
import com.timecapsule.api.common.domain.ConditionType;
import java.time.Instant;
import java.util.UUID;

public record ConditionResponse(
		UUID id,
		UUID capsuleId,
		ConditionType type,
		ConditionResultStatus status,
		String config,
		Instant createdAt,
		Instant evaluatedAt,
		Instant satisfiedAt) {
}
