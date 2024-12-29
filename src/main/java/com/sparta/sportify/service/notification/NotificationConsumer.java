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

    private final SseEmitterService sseEmitterService;
    private final NotificationRepository notificationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

    @KafkaListener(topics = "match-notifications", groupId = "notification-group")
    public void consumeNotification(String message) {
        log.info("Received Kafka notification message: {}", message);

        try {
            // 메시지에서 사용자 ID 추출
            Long userId = extractUserIdFromMessage(message);

            if (userId == null) {
                log.error("User ID is missing in the message: {}", message);
                return; // userId가 없으면 처리하지 않음
            }

            // DB에 저장할 Notification 객체 생성
            Notification notification = new Notification();
            notification.setType("MATCH");
            notification.setStatus(NotificationStatus.PENDING);
            notification.setDeliveryMethod("PUSH");
            notification.setMessage(message);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setUserId(userId); // 동적으로 받은 userId 설정

            // Notification 객체 저장
            notificationRepository.save(notification);
            log.info("Saved notification to database: {}", notification);
        } catch (Exception e) {
            log.error("Failed to process notification message: {}", message, e);
        }
    }

    // 메시지에서 userId를 추출하는 메서드
    private Long extractUserIdFromMessage(String message) {
        try {
            // 메시지가 JSON 형식일 경우
            JsonNode rootNode = objectMapper.readTree(message); // JSON 파싱
            JsonNode userIdNode = rootNode.path("userId"); // userId 추출

            if (!userIdNode.isMissingNode()) {
                return userIdNode.asLong(); // userId가 있으면 반환
            }
        } catch (IOException e) {
            log.warn("Error parsing message to extract userId (may not be in JSON format): {}", message, e);
        }

        // 메시지가 JSON 형식이 아니라면 기본값을 반환하거나 다른 방법으로 처리
        return null; // userId가 없거나 파싱 오류 발생 시 null 반환
    }
}
