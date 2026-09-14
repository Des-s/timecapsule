package com.timecapsule.api.auth.web;

import java.util.UUID;

public record UserResponse(UUID id, String email, String displayName, boolean emailVerified) {
}
