package com.timecapsule.api.capsule;

import com.timecapsule.api.audit.AuditService;
import com.timecapsule.api.auth.domain.UserRepository;
import com.timecapsule.api.capsule.domain.CapsuleEntity;
import com.timecapsule.api.capsule.domain.CapsuleRepository;
import com.timecapsule.api.capsule.web.CapsuleResponse;
import com.timecapsule.api.capsule.web.CreateCapsuleRequest;
import com.timecapsule.api.capsule.web.UpdateCapsuleRequest;
import com.timecapsule.api.common.domain.CapsuleEventType;
import com.timecapsule.api.common.domain.CapsuleStatus;
import com.timecapsule.api.common.web.ApiException;
import com.timecapsule.api.evaluation.CapsuleEvaluationService;
import com.timecapsule.api.evaluation.ConditionEvaluationContext;
import com.timecapsule.api.condition.domain.ConditionEntity;
import com.timecapsule.api.condition.domain.ConditionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CapsuleService {

	private final CapsuleRepository capsuleRepository;
	private final ConditionRepository conditionRepository;
	private final UserRepository userRepository;
	private final AuditService auditService;
	private final CapsuleEvaluationService capsuleEvaluationService;

	public CapsuleService(
			CapsuleRepository capsuleRepository,
			ConditionRepository conditionRepository,
			UserRepository userRepository,
			AuditService auditService,
			CapsuleEvaluationService capsuleEvaluationService) {
		this.capsuleRepository = capsuleRepository;
		this.conditionRepository = conditionRepository;
		this.userRepository = userRepository;
		this.auditService = auditService;
		this.capsuleEvaluationService = capsuleEvaluationService;
	}

	@Transactional
	public CapsuleResponse create(UUID creatorId, CreateCapsuleRequest request) {
		UUID recipientId = request.recipientId() != null ? request.recipientId() : creatorId;
		ensureUserExists(recipientId);

		Instant now = Instant.now();
		CapsuleEntity capsule = new CapsuleEntity(
				UUID.randomUUID(),
				creatorId,
				recipientId,
				request.title().trim(),
				request.content(),
				now);
		if (request.encryptionVersion() != null && request.encryptionVersion() > 0) {
			capsule.setEncryptionVersion(request.encryptionVersion());
		}
		capsuleRepository.save(capsule);
		auditService.record(capsule.getId(), CapsuleEventType.CREATED, creatorId, Map.of("title", capsule.getTitle()));
		return toResponse(capsule, creatorId);
	}

	@Transactional(readOnly = true)
	public List<CapsuleResponse> listForUser(UUID userId) {
		return capsuleRepository.findByCreatorIdOrRecipientIdOrderByCreatedAtDesc(userId, userId).stream()
				.map(capsule -> toResponse(capsule, userId))
				.toList();
	}

	@Transactional(readOnly = true)
	public CapsuleResponse get(UUID userId, UUID capsuleId) {
		CapsuleEntity capsule = getAuthorizedCapsule(userId, capsuleId);
		return toResponse(capsule, userId);
	}

	@Transactional
	public CapsuleResponse update(UUID userId, UUID capsuleId, UpdateCapsuleRequest request) {
		CapsuleEntity capsule = getAuthorizedCapsule(userId, capsuleId);
		ensureDraft(capsule);
		if (!capsule.getCreatorId().equals(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the creator can edit a draft capsule");
		}
		if (request.title() != null) {
			capsule.setTitle(request.title().trim());
		}
		if (request.content() != null) {
			capsule.setEncryptedContent(request.content());
		}
		capsuleRepository.save(capsule);
		auditService.record(capsuleId, CapsuleEventType.UPDATED, userId, Map.of());
		return toResponse(capsule, userId);
	}

	@Transactional
	public void delete(UUID userId, UUID capsuleId) {
		CapsuleEntity capsule = getAuthorizedCapsule(userId, capsuleId);
		ensureDraft(capsule);
		if (!capsule.getCreatorId().equals(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the creator can delete a draft capsule");
		}
		capsuleRepository.delete(capsule);
	}

	@Transactional
	public CapsuleResponse lock(UUID userId, UUID capsuleId, String recipientKeyEnvelope) {
		CapsuleEntity capsule = getAuthorizedCapsule(userId, capsuleId);
		ensureDraft(capsule);
		if (!capsule.getCreatorId().equals(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the creator can lock a capsule");
		}
		List<ConditionEntity> conditions = conditionRepository.findByCapsuleIdOrderByCreatedAtAsc(capsuleId);
		if (conditions.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "NO_CONDITIONS", "Add at least one condition before locking");
		}
		if (capsule.getEncryptionVersion() >= 1) {
			if (recipientKeyEnvelope == null || recipientKeyEnvelope.isBlank()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "MISSING_KEY_ENVELOPE", "Encrypted capsules require a recipient key envelope when locking");
			}
			capsule.setRecipientKeyEnvelope(recipientKeyEnvelope);
		}

		Instant now = Instant.now();
		capsule.setStatus(CapsuleStatus.LOCKED);
		capsule.setLockedAt(now);
		capsule.setNextEvaluationAt(capsuleEvaluationService.computeNextEvaluationAt(conditions, now));
		capsuleRepository.save(capsule);
		auditService.record(capsuleId, CapsuleEventType.LOCKED, userId, Map.of());

		capsuleEvaluationService.processCapsule(
				capsuleId,
				ConditionEvaluationContext.serverDefault(capsule.getRecipientId(), userId));
		return toResponse(capsuleRepository.findById(capsuleId).orElseThrow(), userId);
	}

	@Transactional
	public CapsuleResponse open(UUID userId, UUID capsuleId) {
		CapsuleEntity capsule = getAuthorizedCapsule(userId, capsuleId);
		if (!capsule.getRecipientId().equals(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Only the recipient can open this capsule");
		}
		if (capsule.getStatus() == CapsuleStatus.LOCKED) {
			capsuleEvaluationService.processCapsule(
					capsuleId,
					ConditionEvaluationContext.serverDefault(capsule.getRecipientId(), userId));
			capsule = capsuleRepository.findById(capsuleId).orElseThrow();
		}
		if (capsule.getStatus() != CapsuleStatus.UNLOCKED) {
			throw new ApiException(HttpStatus.CONFLICT, "NOT_UNLOCKED", "Capsule conditions are not yet satisfied");
		}
		auditService.record(capsuleId, CapsuleEventType.OPENED, userId, Map.of());
		return toResponse(capsule, userId, true);
	}

	private CapsuleEntity getAuthorizedCapsule(UUID userId, UUID capsuleId) {
		CapsuleEntity capsule = capsuleRepository.findById(capsuleId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAPSULE_NOT_FOUND", "Capsule not found"));
		if (!capsule.getCreatorId().equals(userId) && !capsule.getRecipientId().equals(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to this capsule");
		}
		return capsule;
	}

	private void ensureDraft(CapsuleEntity capsule) {
		if (capsule.getStatus() != CapsuleStatus.DRAFT) {
			throw new ApiException(HttpStatus.CONFLICT, "NOT_DRAFT", "Capsule is no longer editable");
		}
	}

	private void ensureUserExists(UUID userId) {
		if (!userRepository.existsById(userId)) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "RECIPIENT_NOT_FOUND", "Recipient user does not exist");
		}
	}

	private CapsuleResponse toResponse(CapsuleEntity capsule, UUID viewerId) {
		boolean includePlaintext = capsule.getEncryptionVersion() == 0 && capsule.getStatus() == CapsuleStatus.UNLOCKED;
		return toResponse(capsule, viewerId, includePlaintext);
	}

	private CapsuleResponse toResponse(CapsuleEntity capsule, UUID viewerId, boolean includePlaintextContent) {
		boolean isCreator = capsule.getCreatorId().equals(viewerId);
		boolean isRecipient = capsule.getRecipientId().equals(viewerId);
		boolean draftForCreator = capsule.getStatus() == CapsuleStatus.DRAFT && isCreator;
		boolean unlocked = capsule.getStatus() == CapsuleStatus.UNLOCKED;

		String encryptedPayload = null;
		if (capsule.getEncryptionVersion() >= 1 && (draftForCreator || unlocked)) {
			encryptedPayload = capsule.getEncryptedContent();
		}

		String plaintext = null;
		if (capsule.getEncryptionVersion() == 0 && includePlaintextContent && unlocked) {
			plaintext = capsule.getEncryptedContent();
		}

		String keyEnvelope = null;
		if (capsule.getEncryptionVersion() >= 1 && unlocked && isRecipient) {
			keyEnvelope = capsule.getRecipientKeyEnvelope();
		}

		return new CapsuleResponse(
				capsule.getId(),
				capsule.getCreatorId(),
				capsule.getRecipientId(),
				capsule.getTitle(),
				plaintext,
				encryptedPayload,
				keyEnvelope,
				capsule.getEncryptionVersion(),
				capsule.getStatus(),
				capsule.getLogicOperator(),
				capsule.getLockedAt(),
				capsule.getUnlockedAt(),
				capsule.getCreatedAt());
	}
}
