package com.timecapsule.api.capsule.web;

import jakarta.validation.constraints.Size;

public record UpdateCapsuleRequest(@Size(max = 200) String title, String content) {
}
