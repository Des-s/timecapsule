package com.timecapsule.api.audit.web;

import com.timecapsule.api.audit.domain.CapsuleEventEntity;
import com.timecapsule.api.audit.domain.CapsuleEventRepository;
import com.timecapsule.api.capsule.domain.CapsuleEntity;
import com.timecapsule.api.capsule.domain.CapsuleRepository;
import com.timecapsule.api.common.web.ApiException;
import com.timecapsule.api.common.web.AuthenticatedUser;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/capsules/{capsuleId}/events")
public class AuditController {

	private final CapsuleRepository capsuleRepository;
	private final CapsuleEventRepository capsuleEventRepository;

	public AuditController(CapsuleRepository capsuleRepository, CapsuleEventRepository capsuleEventRepository) {
		this.capsuleRepository = capsuleRepository;
		this.capsuleEventRepository = capsuleEventRepository;
	}

	@GetMapping
	public List<CapsuleEventResponse> list(@PathVariable UUID capsuleId) {
		UUID userId = AuthenticatedUser.requireUserId();
		CapsuleEntity capsule = capsuleRepository.findById(capsuleId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CAPSULE_NOT_FOUND", "Capsule not found"));
		if (!capsule.getCreatorId().equals(userId) && !capsule.getRecipientId().equals(userId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have access to this capsule");
		}
		return capsuleEventRepository.findByCapsuleIdOrderByCreatedAtAsc(capsuleId).stream()
				.map(this::toResponse)
				.toList();
	}

	private CapsuleEventResponse toResponse(CapsuleEventEntity event) {
		return new CapsuleEventResponse(
				event.getId(),
				event.getCapsuleId(),
				event.getEventType(),
				event.getActorId(),
				event.getMetadata(),
				event.getCreatedAt());
	}
}
