package com.timecapsule.api.evaluation;

import java.util.UUID;

public record ConditionEvaluationContext(
		InstantSource instantSource,
		UUID recipientId,
		UUID authenticatedUserId,
		LocationReading locationReading) {

	public static ConditionEvaluationContext serverDefault(UUID recipientId, UUID authenticatedUserId) {
		return new ConditionEvaluationContext(InstantSource.systemUtc(), recipientId, authenticatedUserId, null);
	}

	public static ConditionEvaluationContext scheduled() {
		return new ConditionEvaluationContext(InstantSource.systemUtc(), null, null, null);
	}

	public record LocationReading(double latitude, double longitude, Double accuracyMeters) {
	}
}
