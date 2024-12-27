package com.sparta.sportify.service.notification;

import com.sparta.sportify.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class NotificationController {
    private final SseEmitterService sseEmitterService;
    private final NotificationRepository notificationRepository;
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

    // DB에 저장된 알림 조회
    @GetMapping("/notifications/history")
    public ResponseEntity<List<NotificationResponseDto>> getNotifications() {
        List<NotificationResponseDto> notifications = notificationRepository.findAll()
                .stream()
                .map(notification -> new NotificationResponseDto(
                        notification.getId(),
                        notification.getMessage(),
                        notification.getTimestamp()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(notifications);
    }
}
