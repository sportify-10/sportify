package com.sparta.sportify.service.notification;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final SseEmitterService sseEmitterService;

    // SseEmitterService 주입
    public KafkaConsumerService(SseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

    // Kafka에서 메시지를 받으면 클라이언트에게 전송
    @KafkaListener(topics = "match-notifications", groupId = "your-consumer-group-id")
    public void listen(String message) {
        System.out.println("Kafka에서 받은 메시지: " + message);
        // Kafka에서 받은 메시지를 연결된 모든 클라이언트에 전송
        sseEmitterService.sendToAll(message);
    }
}
