package com.sparta.sportify.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final SseEmitterService sseEmitterService;

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
}
