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
    private final SseEmitterService sseEmitterService;  // SseEmitterService 주입


    // 특정 키와 메시지로 Kafka에 알림 전송
    public void sendMatchNotification(String key, String message) {
        kafkaTemplate.send(TOPIC_NAME, key, message);
    }

    // 특정 사용자에게 알림 전송 및 DB에 저장
    public void sendUserNotification(Long userId, String message) {
        // Kafka에 메시지 전송
        kafkaTemplate.send(TOPIC_NAME, message);

        // 알림 엔티티 생성 및 초기화
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .type("MATCH") // 알림 타입 설정
                .status(Notification.NotificationStatus.PENDING) // 초기 상태 설정
                .deliveryMethod("PUSH") // 전달 방법 설정
                .createdAt(LocalDateTime.now()) // 생성 시간 설정
                .build();

        // DB에 알림 저장
        notificationRepository.save(notification);

        // SSE로 사용자에게 알림 전송
        sseEmitterService.sendToUser(userId, message);
    }
}
