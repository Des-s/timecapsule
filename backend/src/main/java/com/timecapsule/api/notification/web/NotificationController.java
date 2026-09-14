package com.timecapsule.api.notification.web;

import com.timecapsule.api.common.web.AuthenticatedUser;
import com.timecapsule.api.notification.NotificationRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationRegistrationService notificationRegistrationService;

	public NotificationController(NotificationRegistrationService notificationRegistrationService) {
		this.notificationRegistrationService = notificationRegistrationService;
	}

	@PostMapping("/register-token")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void registerToken(@Valid @RequestBody RegisterPushTokenRequest request) {
		notificationRegistrationService.register(AuthenticatedUser.requireUserId(), request);
	}
}
