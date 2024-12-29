package com.sparta.sportify.service.notification;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEmitterService {

    // 연결된 클라이언트 관리
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // 새로운 클라이언트 연결 시 SseEmitter 반환
    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(60 * 1000L); // 60초 타임아웃 설정
        emitters.add(emitter);

        // 클라이언트 연결 종료 시 emitters에서 제거
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    // 연결된 모든 클라이언트에 메시지 전송 (비동기 처리)
    @Async
    public void sendToAll(String message) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("match-notifications").data(message));
            } catch (IOException e) {
                emitters.remove(emitter); // 전송 실패 시 클라이언트 제거
            }
        }
    }
}
