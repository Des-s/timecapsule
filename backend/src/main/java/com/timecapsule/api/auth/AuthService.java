package com.timecapsule.api.auth;

import com.timecapsule.api.auth.domain.UserEntity;
import com.timecapsule.api.auth.domain.UserRepository;
import com.timecapsule.api.auth.security.JwtService;
import com.timecapsule.api.auth.web.AuthResponse;
import com.timecapsule.api.auth.web.LoginRequest;
import com.timecapsule.api.auth.web.RegisterRequest;
import com.timecapsule.api.auth.web.UserResponse;
import com.timecapsule.api.common.web.ApiException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public AuthResponse register(RegisterRequest request) {
		if (userRepository.existsByEmailIgnoreCase(request.email())) {
			throw new ApiException(HttpStatus.CONFLICT, "EMAIL_IN_USE", "Email is already registered");
		}
		Instant now = Instant.now();
		UserEntity user = new UserEntity(
				UUID.randomUUID(),
				request.email().trim().toLowerCase(),
				passwordEncoder.encode(request.password()),
				request.displayName().trim(),
				now);
		userRepository.save(user);
		String token = jwtService.generateToken(user.getId(), user.getEmail());
		return new AuthResponse(token, toUserResponse(user));
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		UserEntity user = userRepository.findByEmailIgnoreCase(request.email().trim())
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password"));
		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password");
		}
		String token = jwtService.generateToken(user.getId(), user.getEmail());
		return new AuthResponse(token, toUserResponse(user));
	}

	@Transactional(readOnly = true)
	public UserResponse lookupByEmail(String email) {
		UserEntity user = userRepository.findByEmailIgnoreCase(email.trim())
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "No user with that email"));
		return toUserResponse(user);
	}

	@Transactional(readOnly = true)
	public UserResponse getUser(UUID userId) {
		UserEntity user = userRepository.findById(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found"));
		return toUserResponse(user);
	}

	private UserResponse toUserResponse(UserEntity user) {
		return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.isEmailVerified());
	}
}
