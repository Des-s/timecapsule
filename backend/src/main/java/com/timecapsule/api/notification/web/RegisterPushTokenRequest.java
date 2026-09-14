package com.timecapsule.api.notification.web;

import jakarta.validation.constraints.NotBlank;

public record RegisterPushTokenRequest(@NotBlank String expoPushToken, String platform) {
}
