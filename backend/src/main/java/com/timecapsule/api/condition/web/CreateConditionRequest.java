package com.timecapsule.api.condition.web;

import com.timecapsule.api.common.domain.ConditionType;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CreateConditionRequest(@NotNull ConditionType type, Map<String, Object> config) {
}
