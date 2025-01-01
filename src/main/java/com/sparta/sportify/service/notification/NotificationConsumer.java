package com.sparta.sportify.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.entity.notification.Notification;
import com.sparta.sportify.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeNotification(String message) {
        log.debug("Received notification: {}", message);

        try {
            NotificationPayload payload = parseMessage(message);
            if (payload == null) return;

            if (isNotificationValid(payload)) {
                processNotification(payload);
            }
        } catch (Exception e) {
            log.error("알림 처리 실패: {}", message, e);
        }
    }

    private NotificationPayload parseMessage(String message) {
        try {
            return objectMapper.readValue(message, NotificationPayload.class);
        } catch (IOException e) {
            log.error("메시지 파싱 실패: {}", message, e);
            return null;
        }
    }

    private boolean isNotificationValid(NotificationPayload payload) {
        if (!isValidPayload(payload)) {
            log.error("유효하지 않은 페이로드: {}", payload);
            return false;
        }

        LocalDateTime scheduledTime = extractScheduledTime(payload.getMessage());
        if (scheduledTime == null || LocalDateTime.now().isAfter(scheduledTime)) {
            log.info("만료된 알림: {}", payload);
            return false;
        }

        return true;
    }

    private void processNotification(NotificationPayload payload) {
        saveNotification(payload);
        sendNotification(payload);
    }

    private void saveNotification(NotificationPayload payload) {
        Notification notification = createNotification(payload);
        notificationRepository.save(notification);
        log.debug("알림 저장 완료: {}", notification.getId());
    }

    private void sendNotification(NotificationPayload payload) {
        sseEmitterService.sendToUser(payload.getUserId(), payload.getMessage());
        log.debug("SSE 알림 전송 완료: userId={}", payload.getUserId());
    }

    private LocalDateTime extractScheduledTime(String message) {
        try {
            Matcher matcher = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")
                    .matcher(message);
            if (matcher.find()) {
                return LocalDateTime.parse(matcher.group());
            }
            return null;
        } catch (Exception e) {
            log.error("시간 추출 실패: {}", message, e);
            return null;
        }
    }

    private Notification createNotification(NotificationPayload payload) {
        return Notification.builder()
                .userId(payload.getUserId())
                .message(payload.getMessage())
                .type("MATCH")
                .status(Notification.NotificationStatus.PENDING)
                .deliveryMethod("PUSH")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private boolean isValidPayload(NotificationPayload payload) {
        return payload != null
                && payload.getUserId() != null
                && StringUtils.hasText(payload.getMessage());
    }
}