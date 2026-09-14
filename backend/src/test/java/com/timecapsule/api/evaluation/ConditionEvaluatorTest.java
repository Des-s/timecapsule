package com.timecapsule.api.evaluation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timecapsule.api.capsule.domain.CapsuleEntity;
import com.timecapsule.api.common.domain.ConditionResultStatus;
import com.timecapsule.api.common.domain.ConditionType;
import com.timecapsule.api.common.domain.LogicOperator;
import com.timecapsule.api.condition.domain.ConditionEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConditionEvaluatorTest {

	private final ConditionEvaluator evaluator = new ConditionEvaluator(new ObjectMapper().findAndRegisterModules());

	@Test
	void dateConditionBeforeUnlockIsFalse() {
		Instant unlockAt = Instant.parse("2026-12-25T00:00:00Z");
		ConditionEntity condition = condition(
				ConditionType.DATE,
				"{\"unlockAt\":\"2026-12-25T00:00:00Z\"}");
		ConditionEvaluationContext context = new ConditionEvaluationContext(
				() -> unlockAt.minusSeconds(1),
				UUID.randomUUID(),
				null,
				null);

		assertEquals(ConditionResultStatus.FALSE, evaluator.evaluate(condition, context));
	}

	@Test
	void andLogicUnlocksWhenAllTrue() {
		UUID recipientId = UUID.randomUUID();
		CapsuleEntity capsule = new CapsuleEntity(
				UUID.randomUUID(),
				UUID.randomUUID(),
				recipientId,
				"title",
				"content",
				Instant.now());
		capsule.setLogicOperator(LogicOperator.AND);

		List<ConditionEntity> conditions = List.of(
				condition(ConditionType.DATE, "{\"unlockAt\":\"2026-01-01T00:00:00Z\"}"),
				condition(ConditionType.IDENTITY, "{}"));

		ConditionEvaluationContext context = new ConditionEvaluationContext(
				() -> Instant.parse("2026-02-01T00:00:00Z"),
				recipientId,
				recipientId,
				null);

		assertEquals(EvaluationOutcome.UNLOCKABLE, evaluator.evaluateCapsule(capsule, conditions, context));
	}

	@Test
	void orLogicUnlocksWhenAnyTrue() {
		CapsuleEntity capsule = new CapsuleEntity(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"title",
				"content",
				Instant.now());
		capsule.setLogicOperator(LogicOperator.OR);

		List<ConditionEntity> conditions = List.of(
				condition(ConditionType.DATE, "{\"unlockAt\":\"2026-12-25T00:00:00Z\"}"),
				condition(ConditionType.IDENTITY, "{}"));

		ConditionEvaluationContext context = new ConditionEvaluationContext(
				() -> Instant.parse("2026-01-01T00:00:00Z"),
				capsule.getRecipientId(),
				capsule.getRecipientId(),
				null);

		assertEquals(EvaluationOutcome.UNLOCKABLE, evaluator.evaluateCapsule(capsule, conditions, context));
	}

	private ConditionEntity condition(ConditionType type, String config) {
		return new ConditionEntity(UUID.randomUUID(), UUID.randomUUID(), type, config, Instant.now());
	}
}
