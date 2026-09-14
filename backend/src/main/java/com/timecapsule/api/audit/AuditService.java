package com.timecapsule.api.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timecapsule.api.audit.domain.CapsuleEventEntity;
import com.timecapsule.api.audit.domain.CapsuleEventRepository;
import com.timecapsule.api.common.domain.CapsuleEventType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

	private final CapsuleEventRepository capsuleEventRepository;
	private final ObjectMapper objectMapper;

	public AuditService(CapsuleEventRepository capsuleEventRepository, ObjectMapper objectMapper) {
		this.capsuleEventRepository = capsuleEventRepository;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public void record(UUID capsuleId, CapsuleEventType eventType, UUID actorId, Map<String, Object> metadata) {
		String metadataJson = null;
		if (metadata != null && !metadata.isEmpty()) {
			try {
				metadataJson = objectMapper.writeValueAsString(metadata);
			}
			catch (JsonProcessingException ex) {
				metadataJson = "{\"error\":\"metadata serialization failed\"}";
			}
		}
		capsuleEventRepository.save(new CapsuleEventEntity(
				UUID.randomUUID(),
				capsuleId,
				eventType,
				actorId,
				metadataJson,
				Instant.now()));
	}
}
