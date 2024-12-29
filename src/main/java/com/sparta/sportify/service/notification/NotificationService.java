package com.sparta.sportify.service.notification;

import com.sparta.sportify.entity.notification.Notification;
import com.sparta.sportify.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC_NAME = "match-notifications";
    private final NotificationRepository notificationRepository;

    public void sendMatchNotification(String key, String message) {
        kafkaTemplate.send(TOPIC_NAME, key, message);
    }

    public void sendUserNotification(String message) {
        kafkaTemplate.send("match-notifications", message);

        // 예시로 userId를 1로 설정, 실제로는 적절한 userId를 가져와야 합니다.
        Long userId = 1L; // 예시 userId (실제 값으로 변경 필요)

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType("MATCH"); // 알림 타입을 "MATCH"로 설정
        notification.setStatus(Notification.NotificationStatus.PENDING);
        notification.setDeliveryMethod("PUSH");
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);  // 알림 저장
    }
}

