package com.timecapsule.api.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.timecapsule.api.capsule.domain.CapsuleEntity;
import com.timecapsule.api.notification.domain.DevicePushTokenEntity;
import com.timecapsule.api.notification.domain.DevicePushTokenRepository;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PushNotificationService {

	private static final Logger log = LoggerFactory.getLogger(PushNotificationService.class);

	private final DevicePushTokenRepository devicePushTokenRepository;
	private final RestClient restClient;
	private final ObjectMapper objectMapper;

	public PushNotificationService(
			DevicePushTokenRepository devicePushTokenRepository,
			ObjectMapper objectMapper,
			@Value("${timecapsule.expo.push-url:https://exp.host/--/api/v2/push/send}") String pushUrl) {
		this.devicePushTokenRepository = devicePushTokenRepository;
		this.objectMapper = objectMapper;
		this.restClient = RestClient.builder().baseUrl(pushUrl).build();
	}

	public void notifyCapsuleUnlocked(CapsuleEntity capsule) {
		List<DevicePushTokenEntity> tokens = devicePushTokenRepository.findByUserId(capsule.getRecipientId());
		if (tokens.isEmpty()) {
			return;
		}

		List<Map<String, Object>> messages = tokens.stream()
				.map(token -> Map.<String, Object>of(
						"to", token.getExpoPushToken(),
						"title", "TimeCapsule unlocked",
						"body", "Your capsule \"" + capsule.getTitle() + "\" is ready to open.",
						"data", Map.of("capsuleId", capsule.getId().toString())))
				.toList();

		try {
			restClient.post()
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.body(messages)
					.retrieve()
					.toBodilessEntity();
		}
		catch (Exception ex) {
			log.warn("Failed to send Expo push for capsule {}: {}", capsule.getId(), ex.getMessage());
		}
	}
}
