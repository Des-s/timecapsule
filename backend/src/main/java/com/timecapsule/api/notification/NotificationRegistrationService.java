package com.timecapsule.api.notification;

import com.timecapsule.api.notification.domain.DevicePushTokenEntity;
import com.timecapsule.api.notification.domain.DevicePushTokenRepository;
import com.timecapsule.api.notification.web.RegisterPushTokenRequest;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationRegistrationService {

	private final DevicePushTokenRepository devicePushTokenRepository;

	public NotificationRegistrationService(DevicePushTokenRepository devicePushTokenRepository) {
		this.devicePushTokenRepository = devicePushTokenRepository;
	}

	@Transactional
	public void register(UUID userId, RegisterPushTokenRequest request) {
		Instant now = Instant.now();
		devicePushTokenRepository.findByUserIdAndExpoPushToken(userId, request.expoPushToken())
				.ifPresentOrElse(existing -> {
					existing.setPlatform(request.platform());
					existing.setUpdatedAt(now);
					devicePushTokenRepository.save(existing);
				}, () -> devicePushTokenRepository.save(new DevicePushTokenEntity(
						UUID.randomUUID(),
						userId,
						request.expoPushToken(),
						request.platform(),
						now)));
	}
}
