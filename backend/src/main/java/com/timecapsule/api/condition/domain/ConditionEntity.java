package com.timecapsule.api.condition.domain;

import com.timecapsule.api.common.domain.ConditionResultStatus;
import com.timecapsule.api.common.domain.ConditionType;
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
@Table(name = "conditions")
public class ConditionEntity {

	@Id
	private UUID id;

	@Column(name = "capsule_id", nullable = false)
	private UUID capsuleId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ConditionType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private ConditionResultStatus status;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private String config;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "evaluated_at")
	private Instant evaluatedAt;

	@Column(name = "satisfied_at")
	private Instant satisfiedAt;

	protected ConditionEntity() {
	}

	public ConditionEntity(UUID id, UUID capsuleId, ConditionType type, String config, Instant now) {
		this.id = id;
		this.capsuleId = capsuleId;
		this.type = type;
		this.status = ConditionResultStatus.PENDING;
		this.config = config;
		this.createdAt = now;
	}

	public UUID getId() {
		return id;
	}

	public UUID getCapsuleId() {
		return capsuleId;
	}

	public ConditionType getType() {
		return type;
	}

	public ConditionResultStatus getStatus() {
		return status;
	}

	public void setStatus(ConditionResultStatus status) {
		this.status = status;
	}

	public String getConfig() {
		return config;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getEvaluatedAt() {
		return evaluatedAt;
	}

	public void setEvaluatedAt(Instant evaluatedAt) {
		this.evaluatedAt = evaluatedAt;
	}

	public Instant getSatisfiedAt() {
		return satisfiedAt;
	}

	public void setSatisfiedAt(Instant satisfiedAt) {
		this.satisfiedAt = satisfiedAt;
	}
}
