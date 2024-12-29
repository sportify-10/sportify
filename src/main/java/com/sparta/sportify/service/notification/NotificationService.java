package com.sparta.sportify.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC_NAME = "match-notifications";

    public void sendMatchNotification(String key, String message) {
        kafkaTemplate.send(TOPIC_NAME, key, message);
    }

    public void sendUserNotification(String message) {
        kafkaTemplate.send("match-notifications", message);
    }
}

