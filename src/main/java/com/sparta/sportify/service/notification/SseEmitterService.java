package com.sparta.sportify.service.notification;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterService {

    // 사용자 ID별 SseEmitter 관리
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 특정 사용자에 대한 SseEmitter 생성
    public SseEmitter createEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(60 * 1000L); // 60초 타임아웃 설정
        emitters.put(userId, emitter);

        // 클라이언트 연결 종료 시 emitters에서 제거
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        return emitter;
    }

    // 특정 사용자에게 메시지 전송
    @Async
    public void sendToUser(Long userId, String message) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name("match-notifications").data(message));
            } catch (IOException e) {
                emitters.remove(userId); // 전송 실패 시 해당 사용자 제거
            }
        }
    }

    // 연결된 모든 클라이언트에 메시지 전송 (기존 메서드)
    @Async
    public void sendToAll(String message) {
        emitters.forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event().name("match-notifications").data(message));
            } catch (IOException e) {
                emitters.remove(userId); // 전송 실패 시 클라이언트 제거
            }
        });
    }
}
