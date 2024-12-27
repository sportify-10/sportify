package com.sparta.sportify.service.notification;

import com.sparta.sportify.entity.notification.Notification;
import com.sparta.sportify.entity.notification.Notification.NotificationStatus;
import com.sparta.sportify.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final SseEmitterService sseEmitterService;
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "match-notification", groupId = "match-notification-group")
    public void consumeMessage(String message) {
        log.info("Received Kafka message: {}", message);

        try {
            // 메시지를 모든 연결된 클라이언트에게 전송
            sseEmitterService.sendToAll(message);
            log.info("Successfully sent message to all clients: {}", message);
        } catch (Exception e) {
            // 오류 발생 시 로그 출력
            log.error("Error sending message to clients: {}", message, e);
        }
    }

    @KafkaListener(topics = "match-notifications", groupId = "notification-group")
    public void consumeNotification(String message) {
        log.info("Received Kafka notification message: {}", message);

        // DB에 저장할 Notification 객체 생성
        Notification notification = new Notification();
        notification.setType("MATCH");
        notification.setStatus(NotificationStatus.PENDING);
        notification.setDeliveryMethod("PUSH");
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUserId(1L); // 예: 기본값 또는 실제 유저 ID 설정

        // Notification 객체 저장
        try {
            notificationRepository.save(notification);
            log.info("Saved notification to database: {}", notification);
        } catch (Exception e) {
            log.error("Failed to save notification to database: {}", notification, e);
        }
    }
}
