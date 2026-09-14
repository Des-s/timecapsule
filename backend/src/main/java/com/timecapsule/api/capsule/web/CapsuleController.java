package com.timecapsule.api.capsule.web;

import com.timecapsule.api.capsule.CapsuleService;
import com.timecapsule.api.common.web.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/capsules")
public class CapsuleController {

	private final CapsuleService capsuleService;

	public CapsuleController(CapsuleService capsuleService) {
		this.capsuleService = capsuleService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CapsuleResponse create(@Valid @RequestBody CreateCapsuleRequest request) {
		return capsuleService.create(AuthenticatedUser.requireUserId(), request);
	}

	@GetMapping
	public List<CapsuleResponse> list() {
		return capsuleService.listForUser(AuthenticatedUser.requireUserId());
	}

	@GetMapping("/{id}")
	public CapsuleResponse get(@PathVariable UUID id) {
		return capsuleService.get(AuthenticatedUser.requireUserId(), id);
	}

	@PatchMapping("/{id}")
	public CapsuleResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCapsuleRequest request) {
		return capsuleService.update(AuthenticatedUser.requireUserId(), id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id) {
		capsuleService.delete(AuthenticatedUser.requireUserId(), id);
	}

	@PostMapping("/{id}/lock")
	public CapsuleResponse lock(@PathVariable UUID id, @RequestBody(required = false) LockCapsuleRequest request) {
		String envelope = request != null ? request.recipientKeyEnvelope() : null;
		return capsuleService.lock(AuthenticatedUser.requireUserId(), id, envelope);
	}

	@PostMapping("/{id}/open")
	public CapsuleResponse open(@PathVariable UUID id) {
		return capsuleService.open(AuthenticatedUser.requireUserId(), id);
	}
}
