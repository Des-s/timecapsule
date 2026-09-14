package com.timecapsule.api.condition.web;

import com.timecapsule.api.common.web.AuthenticatedUser;
import com.timecapsule.api.condition.ConditionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ConditionController {

	private final ConditionService conditionService;

	public ConditionController(ConditionService conditionService) {
		this.conditionService = conditionService;
	}

	@PostMapping("/capsules/{capsuleId}/conditions")
	@ResponseStatus(HttpStatus.CREATED)
	public ConditionResponse add(
			@PathVariable UUID capsuleId,
			@Valid @RequestBody CreateConditionRequest request) {
		return conditionService.add(AuthenticatedUser.requireUserId(), capsuleId, request);
	}

	@GetMapping("/capsules/{capsuleId}/conditions")
	public List<ConditionResponse> list(@PathVariable UUID capsuleId) {
		return conditionService.list(AuthenticatedUser.requireUserId(), capsuleId);
	}

	@DeleteMapping("/capsules/{capsuleId}/conditions/{conditionId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteFromCapsule(@PathVariable UUID capsuleId, @PathVariable UUID conditionId) {
		conditionService.delete(AuthenticatedUser.requireUserId(), capsuleId, conditionId);
	}

	@PostMapping("/capsules/{capsuleId}/location-check")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void locationCheck(@PathVariable UUID capsuleId, @Valid @RequestBody LocationCheckRequest request) {
		conditionService.submitLocationCheck(
				AuthenticatedUser.requireUserId(),
				capsuleId,
				request.latitude(),
				request.longitude(),
				request.accuracyMeters());
	}
}
