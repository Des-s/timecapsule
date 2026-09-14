package com.timecapsule.api.capsule.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateCapsuleRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank String content,
		UUID recipientId,
		Integer encryptionVersion) {
}
