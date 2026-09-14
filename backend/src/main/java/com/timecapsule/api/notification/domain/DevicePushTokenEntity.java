package com.timecapsule.api.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "device_push_tokens")
public class DevicePushTokenEntity {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "expo_push_token", nullable = false, length = 512)
	private String expoPushToken;

	@Column(length = 32)
	private String platform;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected DevicePushTokenEntity() {
	}

	public DevicePushTokenEntity(UUID id, UUID userId, String expoPushToken, String platform, Instant now) {
		this.id = id;
		this.userId = userId;
		this.expoPushToken = expoPushToken;
		this.platform = platform;
		this.createdAt = now;
		this.updatedAt = now;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUserId() {
		return userId;
	}

	public String getExpoPushToken() {
		return expoPushToken;
	}

	public String getPlatform() {
		return platform;
	}

	public void setExpoPushToken(String expoPushToken) {
		this.expoPushToken = expoPushToken;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
