package com.timecapsule.api.evaluation;

import com.timecapsule.api.audit.AuditService;
import com.timecapsule.api.capsule.domain.CapsuleEntity;
import com.timecapsule.api.capsule.domain.CapsuleRepository;
import com.timecapsule.api.common.domain.CapsuleEventType;
import com.timecapsule.api.common.domain.CapsuleStatus;
import com.timecapsule.api.common.domain.ConditionResultStatus;
import com.timecapsule.api.common.domain.ConditionType;
import com.timecapsule.api.condition.domain.ConditionEntity;
import com.timecapsule.api.condition.domain.ConditionRepository;
import com.timecapsule.api.notification.PushNotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CapsuleEvaluationService {

	private static final EnumSet<ConditionType> SCHEDULER_TYPES = EnumSet.of(ConditionType.DATE);

	private final CapsuleRepository capsuleRepository;
	private final ConditionRepository conditionRepository;
	private final ConditionEvaluator conditionEvaluator;
	private final AuditService auditService;
	private final PushNotificationService pushNotificationService;
	private final ObjectMapper objectMapper;

	public CapsuleEvaluationService(
			CapsuleRepository capsuleRepository,
			ConditionRepository conditionRepository,
			ConditionEvaluator conditionEvaluator,
			AuditService auditService,
			PushNotificationService pushNotificationService,
			ObjectMapper objectMapper) {
		this.capsuleRepository = capsuleRepository;
		this.conditionRepository = conditionRepository;
		this.conditionEvaluator = conditionEvaluator;
		this.auditService = auditService;
		this.pushNotificationService = pushNotificationService;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void processCapsule(UUID capsuleId, ConditionEvaluationContext context) {
		CapsuleEntity capsule = capsuleRepository.findById(capsuleId).orElse(null);
		if (capsule == null || capsule.getStatus() != CapsuleStatus.LOCKED) {
			return;
		}

		List<ConditionEntity> conditions = conditionRepository.findByCapsuleIdOrderByCreatedAtAsc(capsuleId);
		Instant now = context.instantSource().now();
		ConditionEvaluationContext evaluationContext = new ConditionEvaluationContext(
				context.instantSource(),
				capsule.getRecipientId(),
				context.authenticatedUserId(),
				context.locationReading());

		for (ConditionEntity condition : conditions) {
			ConditionResultStatus result = conditionEvaluator.evaluate(condition, evaluationContext);
			if (condition.getStatus() != result) {
				condition.setStatus(result);
				condition.setEvaluatedAt(now);
				if (result == ConditionResultStatus.TRUE) {
					condition.setSatisfiedAt(now);
					auditService.record(capsuleId, CapsuleEventType.CONDITION_SATISFIED, context.authenticatedUserId(),
							Map.of("conditionId", condition.getId().toString(), "type", condition.getType().name()));
				}
				conditionRepository.save(condition);
				auditService.record(capsuleId, CapsuleEventType.CONDITION_EVALUATED, context.authenticatedUserId(),
						Map.of("conditionId", condition.getId().toString(), "result", result.name()));
			}
		}

		EvaluationOutcome outcome = conditionEvaluator.evaluateCapsule(capsule, conditions, evaluationContext);
		if (outcome == EvaluationOutcome.UNLOCKABLE) {
			unlockCapsule(capsule, now);
		}
		else {
			capsule.setNextEvaluationAt(computeNextEvaluationAt(conditions, now));
			capsuleRepository.save(capsule);
		}
	}

	@Transactional
	public void evaluateDueLockedCapsules() {
		Instant now = Instant.now();
		List<CapsuleEntity> dueCapsules = capsuleRepository.findDueForEvaluation(CapsuleStatus.LOCKED, now);
		for (CapsuleEntity capsule : dueCapsules) {
			processCapsule(capsule.getId(), ConditionEvaluationContext.scheduled());
		}
	}

	private void unlockCapsule(CapsuleEntity capsule, Instant now) {
		capsule.setStatus(CapsuleStatus.UNLOCKED);
		capsule.setUnlockedAt(now);
		capsule.setNextEvaluationAt(null);
		capsuleRepository.save(capsule);
		auditService.record(capsule.getId(), CapsuleEventType.UNLOCKED, null, Map.of());
		pushNotificationService.notifyCapsuleUnlocked(capsule);
	}

	public Instant computeNextEvaluationAt(List<ConditionEntity> conditions, Instant now) {
		Instant next = null;
		for (ConditionEntity condition : conditions) {
			if (!SCHEDULER_TYPES.contains(condition.getType())) {
				continue;
			}
			if (condition.getStatus() == ConditionResultStatus.TRUE) {
				continue;
			}
			try {
				JsonNode config = objectMapper.readTree(condition.getConfig());
				Instant unlockAt = Instant.parse(config.get("unlockAt").asText());
				if (unlockAt.isAfter(now) && (next == null || unlockAt.isBefore(next))) {
					next = unlockAt;
				}
			}
			catch (Exception ignored) {
				// skip malformed configs
			}
		}
		return next;
	}
}
