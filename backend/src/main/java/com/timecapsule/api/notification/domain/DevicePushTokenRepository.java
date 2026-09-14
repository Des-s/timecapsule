package com.timecapsule.api.notification.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevicePushTokenRepository extends JpaRepository<DevicePushTokenEntity, UUID> {

	List<DevicePushTokenEntity> findByUserId(UUID userId);

	Optional<DevicePushTokenEntity> findByUserIdAndExpoPushToken(UUID userId, String expoPushToken);
}
