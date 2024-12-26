package com.sparta.sportify.service.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class NotificationController {
    private final SseEmitterService sseEmitterService;

    private final NotificationService notificationService;

    @PostMapping("/notifications")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequestDto request) {
        String message = String.format(
                "경기 알림: %s 경기장에서 %s에 시작합니다!",
                request.getStadiumName(),
                request.getStartTime()
        );
        notificationService.sendUserNotification(message);
        return ResponseEntity.ok("Notification sent: " + message);
    }


    @GetMapping("/notifications")
    public SseEmitter subscribeToNotifications() {
        return sseEmitterService.createEmitter();
    }
}
