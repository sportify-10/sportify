package com.sparta.sportify.service.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SseEmitterController {

    @Autowired
    private SseEmitterService sseEmitterService;

    // 특정 사용자에 대한 SSE 연결 요청
    @GetMapping("/sse/{userId}")
    public SseEmitter streamMatchNotifications(@PathVariable Long userId) {
        // 사용자가 연결을 요청하면 새로운 SseEmitter 생성
        return sseEmitterService.createEmitter(userId);
    }

    // 특정 사용자에게 알림 메시지 전송 (테스트용 엔드포인트)
    @GetMapping("/send/{userId}")
    public String sendMatchNotification(@PathVariable Long userId, String message) {
        // 알림을 보내는 메서드 호출
        sseEmitterService.sendToUser(userId, message);
        return "Notification sent to user " + userId;
    }

    // 모든 클라이언트에 알림 메시지 전송 (테스트용 엔드포인트)
    @GetMapping("/sendToAll")
    public String sendToAll(String message) {
        // 모든 사용자에게 알림을 보내는 메서드 호출
        sseEmitterService.sendToAll(message);
        return "Notification sent to all users";
    }
}
