package com.sparta.sportify.service.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
public class SseEmitterController {

    @Autowired
    private SseEmitterService sseEmitterService;

    @Autowired
    private final NotificationConsumer notificationConsumer;

    public SseEmitterController(NotificationConsumer notificationConsumer) {
        this.notificationConsumer = notificationConsumer;
    }

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

    /*
     * 블로그 참고한 테스트 */
    //응답 mime type 은 반드시 text/event-stream 이여야 한다.
    //클라이언트로 부터 SSE subscription 을 수락한다.
    @GetMapping(path = "/v1/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe() {
        String sseId = UUID.randomUUID().toString();
        SseEmitter emitter = sseEmitterService.subscribe(sseId);
        return ResponseEntity.ok(emitter);
    }

    //NotificationPayload 를 SSE 로 연결된 모든 클라이언트에게 broadcasting 한다.
    @PostMapping(path = "/v1/sse/broadcast")
    public ResponseEntity<Void> broadcast(@RequestBody NotificationPayload eventPayload) {
        sseEmitterService.broadcast(eventPayload);
        return ResponseEntity.ok().build();
    }


}
