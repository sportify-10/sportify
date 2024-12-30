package com.sparta.sportify.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.entity.notification.Notification;
import com.sparta.sportify.entity.notification.Notification.NotificationStatus;
import com.sparta.sportify.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final SseEmitterService sseEmitterService; // SSE 전송 서비스
    private final NotificationRepository notificationRepository; // 알림 저장소
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱을 위한 ObjectMapper

    @KafkaListener(topics = "match-notifications", groupId = "notification-group")
    public void consumeNotification(String message) {
        log.info("Received Kafka notification message: {}", message);

        try {
            // 메시지에서 사용자 ID 및 메시지 추출
            Long userId = extractUserIdFromMessage(message);
            String notificationMessage = extractNotificationMessage(message);

            if (userId == null || notificationMessage == null) {
                log.error("Invalid message format or missing data: {}", message);
                return;
            }

            // DB에 저장할 Notification 객체 생성
            Notification notification = new Notification();
            notification.setType("MATCH");
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setDeliveryMethod("PUSH");
            notification.setMessage(notificationMessage);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUserId(userId);

            // Notification 객체 저장
            notificationRepository.save(notification);
            log.info("Saved notification to database: {}", notification);

            // SSE를 통해 클라이언트로 알림 전송
            sseEmitterService.sendToUser(userId, notificationMessage);
            log.info("Notification sent to user {} via SSE: {}", userId, notificationMessage);

        } catch (Exception e) {
            log.error("Failed to process notification message: {}", message, e);
        }
    }

    // 메시지에서 userId를 추출하는 메서드
    private Long extractUserIdFromMessage(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode userIdNode = rootNode.path("userId");

            if (!userIdNode.isMissingNode()) {
                return userIdNode.asLong();
            }
        } catch (IOException e) {
            log.error("Error parsing message to extract userId: {}", message, e);
        }
        return null;
    }

    // 메시지에서 알림 메시지를 추출하는 메서드
    private String extractNotificationMessage(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            JsonNode messageNode = rootNode.path("message");

            if (!messageNode.isMissingNode()) {
                return messageNode.asText();
            }
        } catch (IOException e) {
            log.error("Error parsing message to extract notification message: {}", message, e);
        }
        return null;
    }
}
