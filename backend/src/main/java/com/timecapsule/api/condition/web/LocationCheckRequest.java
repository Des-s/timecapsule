package com.timecapsule.api.condition.web;

import jakarta.validation.constraints.NotNull;

public record LocationCheckRequest(
		@NotNull Double latitude,
		@NotNull Double longitude,
		Double accuracyMeters) {
}
