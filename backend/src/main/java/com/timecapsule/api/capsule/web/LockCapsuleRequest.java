package com.timecapsule.api.capsule.web;

import jakarta.validation.constraints.NotBlank;

public record LockCapsuleRequest(@NotBlank String recipientKeyEnvelope) {
}
