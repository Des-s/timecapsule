package com.timecapsule.api.audit.domain;

import com.timecapsule.api.common.domain.CapsuleEventType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "capsule_events")
public class CapsuleEventEntity {

	@Id
	private UUID id;

	@Column(name = "capsule_id", nullable = false)
	private UUID capsuleId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 64)
	private CapsuleEventType eventType;

	@Column(name = "actor_id")
	private UUID actorId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(columnDefinition = "jsonb")
	private String metadata;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected CapsuleEventEntity() {
	}

	public CapsuleEventEntity(
			UUID id,
			UUID capsuleId,
			CapsuleEventType eventType,
			UUID actorId,
			String metadata,
			Instant now) {
		this.id = id;
		this.capsuleId = capsuleId;
		this.eventType = eventType;
		this.actorId = actorId;
		this.metadata = metadata;
		this.createdAt = now;
	}

	public UUID getId() {
		return id;
	}

	public UUID getCapsuleId() {
		return capsuleId;
	}

	public CapsuleEventType getEventType() {
		return eventType;
	}

	public UUID getActorId() {
		return actorId;
	}

	public String getMetadata() {
		return metadata;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
