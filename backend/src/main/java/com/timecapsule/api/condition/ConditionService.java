package com.timecapsule.api.condition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timecapsule.api.capsule.domain.CapsuleEntity;
import com.timecapsule.api.capsule.domain.CapsuleRepository;
import com.timecapsule.api.common.domain.CapsuleStatus;
import com.timecapsule.api.common.domain.ConditionType;
import com.timecapsule.api.common.web.ApiException;
import com.timecapsule.api.condition.domain.ConditionEntity;
import com.timecapsule.api.condition.domain.ConditionRepository;
import com.timecapsule.api.condition.web.ConditionResponse;
import com.timecapsule.api.condition.web.CreateConditionRequest;
import com.timecapsule.api.evaluation.ConditionEvaluationContext;
import com.timecapsule.api.evaluation.CapsuleEvaluationService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConditionService {

	private final CapsuleRepository capsuleRepository;
	private final ConditionRepository conditionRepository;
	private final ObjectMapper objectMapper;
	private final CapsuleEvaluationService capsuleEvaluationService;

	public ConditionService(
			CapsuleRepository capsuleRepository,
			ConditionRepository conditionRepository,
			ObjectMapper objectMapper,
			CapsuleEvaluationService capsuleEvaluationService) {
		this.capsuleRepository = capsuleRepository;
		this.conditionRepository = conditionRepository;
		this.objectMapper = objectMapper;
		this.capsuleEvaluationService = capsuleEvaluationService;
	}

	@Transactional
	public ConditionResponse add(UUID userId, UUID capsuleId, CreateConditionRequest request) {
		CapsuleEntity capsule = getEditableCapsule(userId, capsuleId);
		validateConfig(request.type(), request.config());
		String configJson = writeConfig(request.config());
		ConditionEntity condition = new ConditionEntity(
				UUID.randomUUID(),
				capsule.getId(),
				request.type(),
				configJson,
				Instant.now());
		conditionRepository.save(condition);
		return toResponse(condition);
	}

	@Transactional(readOnly = true)
	public List<ConditionResponse> list(UUID userId, UUID capsuleId) {
		getAccessibleCapsule(userId, capsuleId);
		return conditionRepository.findByCapsuleIdOrderByCreatedAtAsc(capsuleId).stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public void delete(UUID userId, UUID capsuleId, UUID conditionId) {
		getEditableCapsule(userId, capsuleId);
		int deleted = conditionRepository.deleteByCapsuleIdAndId(capsuleId, conditionId);
		if (deleted == 0) {
			throw new ApiException(HttpStatus.NOT_FOUND, "CONDITION_NOT_FOUND", "Condition not found");
		}
	}

	@Transactional
	public void submitLocationCheck(UUID userId, UUID capsuleId, double latitude, double longitude, Double accuracy) {
		CapsuleEntity capsule = getAccessibleCapsule(userId, capsuleId);
		if (capsule.getStatus() != CapsuleStatus.LOCKED) {
			throw new ApiException(HttpStatus.CONFLICT, "NOT_LOCKED", "Location checks apply to locked capsules");
		}
		var location = new ConditionEvaluationContext.LocationReading(latitude, longitude, accuracy);
		var context = new ConditionEvaluationContext(
				ConditionEvaluationContext.scheduled().instantSource(),
				capsule.getRecipientId(),
				userId,
				location);
		capsuleEvaluationService.processCapsule(capsuleId, context);
	}

	private CapsuleEntity getAccessibleCapsule(UUID userId, UUID capsuleId) {
		CapsuleEntity capsule = capsuleRepository.findById(capsuleId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAPSULE_NOT_FOUND", "Capsule not found"));
		if (!capsule.getCreatorId().equals(userId) && !capsule.getRecipientId().equals(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to this capsule");
		}
		return capsule;
	}

	private CapsuleEntity getEditableCapsule(UUID userId, UUID capsuleId) {
		CapsuleEntity capsule = getAccessibleCapsule(userId, capsuleId);
		if (capsule.getStatus() != CapsuleStatus.DRAFT) {
			throw new ApiException(HttpStatus.CONFLICT, "NOT_DRAFT", "Conditions can only be changed while in draft");
		}
		if (!capsule.getCreatorId().equals(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the creator can modify conditions");
		}
		return capsule;
	}

	private void validateConfig(ConditionType type, Map<String, Object> config) {
		switch (type) {
			case DATE -> requireKeys(config, "unlockAt");
			case LOCATION -> requireKeys(config, "latitude", "longitude", "radiusMeters");
			case IDENTITY, CONNECTIVITY -> {
				// no config required for MVP identity/connectivity markers
			}
		}
	}

	private void requireKeys(Map<String, Object> config, String... keys) {
		for (String key : keys) {
			if (config == null || !config.containsKey(key) || config.get(key) == null) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CONFIG", "Missing config field: " + key);
			}
		}
	}

	private String writeConfig(Map<String, Object> config) {
		try {
			return objectMapper.writeValueAsString(config == null ? Map.of() : config);
		}
		catch (Exception ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CONFIG", "Condition config must be valid JSON");
		}
	}

	private ConditionResponse toResponse(ConditionEntity condition) {
		return new ConditionResponse(
				condition.getId(),
				condition.getCapsuleId(),
				condition.getType(),
				condition.getStatus(),
				condition.getConfig(),
				condition.getCreatedAt(),
				condition.getEvaluatedAt(),
				condition.getSatisfiedAt());
	}
}
