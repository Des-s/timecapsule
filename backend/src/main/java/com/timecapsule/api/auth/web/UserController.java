package com.timecapsule.api.auth.web;

import com.timecapsule.api.auth.AuthService;
import com.timecapsule.api.common.web.AuthenticatedUser;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

	private final AuthService authService;

	public UserController(AuthService authService) {
		this.authService = authService;
	}

	@GetMapping("/lookup")
	public UserResponse lookup(@RequestParam @NotBlank @Email String email) {
		AuthenticatedUser.requireUserId();
		return authService.lookupByEmail(email);
	}
}
