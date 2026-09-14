package com.timecapsule.api.capsule.web;

import com.timecapsule.api.common.domain.CapsuleStatus;
import com.timecapsule.api.common.domain.LogicOperator;
import java.time.Instant;
import java.util.UUID;

public record CapsuleResponse(
		UUID id,
		UUID creatorId,
		UUID recipientId,
		String title,
		String content,
		String encryptedPayload,
		String recipientKeyEnvelope,
		int encryptionVersion,
		CapsuleStatus status,
		LogicOperator logicOperator,
		Instant lockedAt,
		Instant unlockedAt,
		Instant createdAt) {
}
