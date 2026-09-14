package com.timecapsule.api.common.web;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex) {
		return ResponseEntity.status(ex.getStatus())
				.body(Map.of(
						"error", ex.getCode(),
						"message", ex.getMessage(),
						"timestamp", Instant.now().toString()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		return ResponseEntity.badRequest()
				.body(Map.of(
						"error", "VALIDATION_ERROR",
						"message", ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage(),
						"timestamp", Instant.now().toString()));
	}
}
