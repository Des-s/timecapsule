package com.timecapsule.api.capsule.domain;

import com.timecapsule.api.common.domain.CapsuleStatus;
import com.timecapsule.api.common.domain.LogicOperator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "capsules")
public class CapsuleEntity {

	@Id
	private UUID id;

	@Column(name = "creator_id", nullable = false)
	private UUID creatorId;

	@Column(name = "recipient_id", nullable = false)
	private UUID recipientId;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(name = "encrypted_content", nullable = false, columnDefinition = "TEXT")
	private String encryptedContent;

	@Column(name = "content_version", nullable = false)
	private int contentVersion;

	@Column(name = "encryption_version", nullable = false)
	private int encryptionVersion;

	@Column(name = "recipient_key_envelope", columnDefinition = "TEXT")
	private String recipientKeyEnvelope;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CapsuleStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "logic_operator", nullable = false, length = 16)
	private LogicOperator logicOperator;

	@Column(name = "next_evaluation_at")
	private Instant nextEvaluationAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "locked_at")
	private Instant lockedAt;

	@Column(name = "unlocked_at")
	private Instant unlockedAt;

	@Column(name = "expires_at")
	private Instant expiresAt;

	protected CapsuleEntity() {
	}

	public CapsuleEntity(
			UUID id,
			UUID creatorId,
			UUID recipientId,
			String title,
			String encryptedContent,
			Instant now) {
		this.id = id;
		this.creatorId = creatorId;
		this.recipientId = recipientId;
		this.title = title;
		this.encryptedContent = encryptedContent;
		this.contentVersion = 1;
		this.encryptionVersion = 0;
		this.status = CapsuleStatus.DRAFT;
		this.logicOperator = LogicOperator.AND;
		this.createdAt = now;
	}

	public UUID getId() {
		return id;
	}

	public UUID getCreatorId() {
		return creatorId;
	}

	public UUID getRecipientId() {
		return recipientId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEncryptedContent() {
		return encryptedContent;
	}

	public void setEncryptedContent(String encryptedContent) {
		this.encryptedContent = encryptedContent;
	}

	public int getContentVersion() {
		return contentVersion;
	}

	public int getEncryptionVersion() {
		return encryptionVersion;
	}

	public void setEncryptionVersion(int encryptionVersion) {
		this.encryptionVersion = encryptionVersion;
	}

	public String getRecipientKeyEnvelope() {
		return recipientKeyEnvelope;
	}

	public void setRecipientKeyEnvelope(String recipientKeyEnvelope) {
		this.recipientKeyEnvelope = recipientKeyEnvelope;
	}

	public CapsuleStatus getStatus() {
		return status;
	}

	public void setStatus(CapsuleStatus status) {
		this.status = status;
	}

	public LogicOperator getLogicOperator() {
		return logicOperator;
	}

	public void setLogicOperator(LogicOperator logicOperator) {
		this.logicOperator = logicOperator;
	}

	public Instant getNextEvaluationAt() {
		return nextEvaluationAt;
	}

	public void setNextEvaluationAt(Instant nextEvaluationAt) {
		this.nextEvaluationAt = nextEvaluationAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getLockedAt() {
		return lockedAt;
	}

	public void setLockedAt(Instant lockedAt) {
		this.lockedAt = lockedAt;
	}

	public Instant getUnlockedAt() {
		return unlockedAt;
	}

	public void setUnlockedAt(Instant unlockedAt) {
		this.unlockedAt = unlockedAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
