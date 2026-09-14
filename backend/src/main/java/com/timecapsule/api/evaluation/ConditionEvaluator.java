package com.timecapsule.api.evaluation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timecapsule.api.capsule.domain.CapsuleEntity;
import com.timecapsule.api.common.domain.ConditionResultStatus;
import com.timecapsule.api.common.domain.ConditionType;
import com.timecapsule.api.common.domain.LogicOperator;
import com.timecapsule.api.condition.domain.ConditionEntity;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ConditionEvaluator {

	private final ObjectMapper objectMapper;

	public ConditionEvaluator(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public ConditionResultStatus evaluate(ConditionEntity condition, ConditionEvaluationContext context) {
		return switch (condition.getType()) {
			case DATE -> evaluateDate(condition, context.instantSource().now());
			case IDENTITY -> evaluateIdentity(context);
			case LOCATION -> evaluateLocation(condition, context);
			case CONNECTIVITY -> ConditionResultStatus.PENDING;
		};
	}

	public EvaluationOutcome evaluateCapsule(CapsuleEntity capsule, List<ConditionEntity> conditions,
			ConditionEvaluationContext context) {
		if (conditions.isEmpty()) {
			return EvaluationOutcome.UNLOCKABLE;
		}

		List<ConditionResultStatus> results = conditions.stream()
				.map(condition -> evaluate(condition, context))
				.toList();

		if (capsule.getLogicOperator() == LogicOperator.OR) {
			if (results.stream().anyMatch(r -> r == ConditionResultStatus.TRUE)) {
				return EvaluationOutcome.UNLOCKABLE;
			}
			if (results.stream().allMatch(r -> r == ConditionResultStatus.FALSE)) {
				return EvaluationOutcome.LOCKED;
			}
			return EvaluationOutcome.WAITING;
		}

		if (results.stream().anyMatch(r -> r == ConditionResultStatus.FALSE)) {
			return EvaluationOutcome.LOCKED;
		}
		if (results.stream().anyMatch(r -> r == ConditionResultStatus.PENDING)) {
			return EvaluationOutcome.WAITING;
		}
		return EvaluationOutcome.UNLOCKABLE;
	}

	private ConditionResultStatus evaluateDate(ConditionEntity condition, Instant now) {
		try {
			JsonNode config = objectMapper.readTree(condition.getConfig());
			Instant unlockAt = Instant.parse(config.get("unlockAt").asText());
			return now.isBefore(unlockAt) ? ConditionResultStatus.FALSE : ConditionResultStatus.TRUE;
		}
		catch (Exception ex) {
			return ConditionResultStatus.FALSE;
		}
	}

	private ConditionResultStatus evaluateIdentity(ConditionEvaluationContext context) {
		if (context.authenticatedUserId() == null || context.recipientId() == null) {
			return ConditionResultStatus.PENDING;
		}
		return context.recipientId().equals(context.authenticatedUserId())
				? ConditionResultStatus.TRUE
				: ConditionResultStatus.FALSE;
	}

	private ConditionResultStatus evaluateLocation(ConditionEntity condition, ConditionEvaluationContext context) {
		if (context.locationReading() == null) {
			return ConditionResultStatus.PENDING;
		}
		try {
			JsonNode config = objectMapper.readTree(condition.getConfig());
			double targetLat = config.get("latitude").asDouble();
			double targetLon = config.get("longitude").asDouble();
			double radiusMeters = config.get("radiusMeters").asDouble();
			double distance = Haversine.distanceMeters(
					context.locationReading().latitude(),
					context.locationReading().longitude(),
					targetLat,
					targetLon);
			return distance <= radiusMeters ? ConditionResultStatus.TRUE : ConditionResultStatus.FALSE;
		}
		catch (Exception ex) {
			return ConditionResultStatus.FALSE;
		}
	}
}
