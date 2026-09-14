package com.timecapsule.api.auth.security;

import com.timecapsule.api.common.config.TimecapsuleProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	private final SecretKey secretKey;
	private final long expirationMinutes;

	public JwtService(TimecapsuleProperties properties) {
		this.secretKey = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
		this.expirationMinutes = properties.jwt().expirationMinutes();
	}

	public String generateToken(UUID userId, String email) {
		Instant now = Instant.now();
		Instant expiry = now.plusSeconds(expirationMinutes * 60);
		return Jwts.builder()
				.subject(userId.toString())
				.claim("email", email)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiry))
				.signWith(secretKey)
				.compact();
	}

	public UUID parseUserId(String token) {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return UUID.fromString(claims.getSubject());
	}
}
