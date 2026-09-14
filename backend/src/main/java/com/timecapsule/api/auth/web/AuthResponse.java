package com.timecapsule.api.auth.web;

public record AuthResponse(String accessToken, UserResponse user) {
}
