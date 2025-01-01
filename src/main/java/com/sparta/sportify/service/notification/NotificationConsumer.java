package com.sparta.sportify.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.entity.notification.Notification;
import com.sparta.sportify.repository.NotificationRepository;
import lombok.Getter;
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

    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SseEmitterService sseEmitterService; // SSE 전송 서비스 추가

    // 최신 Kafka 메시지를 저장하기 위한 필드 (volatile 제거)
    private String latestKafkaMessage;

    @KafkaListener(topics = "match-notifications", groupId = "notification-group")
    public void consumeNotification(String message) {
        log.info("Received Kafka notification message: {}", message);

        // 최신 메시지 업데이트
        latestKafkaMessage = message;
        log.info("Updated latestKafkaMessage: {}", latestKafkaMessage);

        try {
            // 메시지에서 userId, message 및 scheduledTime 추출
            Long userId = extractUserIdFromMessage(message);
            String notificationMessage = extractNotificationMessage(message);
            LocalDateTime scheduledTime = extractScheduledTimeFromMessage(notificationMessage);

            // 알림 내용이 없거나 필요한 정보가 부족한 경우 처리
            if (userId == null || notificationMessage == null || scheduledTime == null) {
                log.error("Invalid message format or missing data: {}", message);
                return;
            }

            // 현재 시간이 예정된 경기 시간이 지나지 않았는지 확인
            if (LocalDateTime.now().isAfter(scheduledTime)) {
                log.info("The scheduled match time has passed. Notification not sent.");
                return;  // 예정된 시간 이후에는 알림을 보내지 않음
            }

            // 알림 DB 저장
            Notification notification = Notification.builder()
                    .userId(userId)
                    .message(notificationMessage)
                    .type("MATCH")
                    .status(Notification.NotificationStatus.PENDING)
                    .deliveryMethod("PUSH")
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

            log.info("Saved notification to database: {}", notification);

            // SSE를 통해 클라이언트로 알림 전송
            sseEmitterService.sendToUser(userId, notificationMessage);
            log.info("Notification sent to user {} via SSE: {}", userId, notificationMessage);

        } catch (Exception e) {
            log.error("Failed to process notification message: {}", message, e);
        }
    }

    // 메시지에서 userId 추출
    private Long extractUserIdFromMessage(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            return rootNode.path("userId").asLong();
        } catch (IOException e) {
            log.error("Error parsing message to extract userId: {}", message, e);
            return null;
        }
    }

    // 메시지에서 알림 내용 추출
    private String extractNotificationMessage(String message) {
        try {
            JsonNode rootNode = objectMapper.readTree(message);
            return rootNode.path("message").asText();
        } catch (IOException e) {
            log.error("Error parsing message to extract notification message: {}", message, e);
            return null;
        }
    }

    // 메시지에서 예정된 경기 시간 추출 (경기 시간 파싱 개선)
    private LocalDateTime extractScheduledTimeFromMessage(String messageText) {
        try {
            // 메시지에서 경기 시간이 "2024-12-31T14:00"과 같은 형식으로 포함된다고 가정
            int startIdx = messageText.indexOf("2024");  // 경기 시작 시간을 포함하는 부분의 시작
            int endIdx = messageText.indexOf("에"); // "에" 뒤로 끝나는 시점
            if (startIdx == -1 || endIdx == -1) {
                log.error("Invalid format for scheduled time in message: {}", messageText);
                return null;
            }

            String dateTimeString = messageText.substring(startIdx, endIdx).trim();
            return LocalDateTime.parse(dateTimeString);
        } catch (Exception e) {
            log.error("Error parsing scheduled time from message: {}", messageText, e);
            return null;
        }
    }
}
