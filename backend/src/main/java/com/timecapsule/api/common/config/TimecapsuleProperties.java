package com.timecapsule.api.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "timecapsule")
public record TimecapsuleProperties(Jwt jwt, Evaluation evaluation) {

	public record Jwt(String secret, long expirationMinutes) {
	}

	public record Evaluation(long fixedDelayMs) {
	}
}
