package com.sparta.sportify.service.notification;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // 새로운 클라이언트 연결 시 SseEmitter를 반환
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        return emitter;
    }

    // 모든 연결된 클라이언트에 메시지 전송
    public void sendToAll(String message) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(message);
            } catch (Exception e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}
