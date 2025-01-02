package com.sparta.sportify.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SseEmitterService {

    // 사용자 ID별 SseEmitter 관리
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**/
    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
    private static final long TIMEOUT = 60 * 1000;
    private static final long RECONNECTION_TIMEOUT = 1000L;

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

    /*
     * 블로그 참고
     * */
    public SseEmitter subscribe(String id) {
        SseEmitter emitter = createEmitter();
        //연결 세션 timeout 이벤트 핸들러 등록
        emitter.onTimeout(() -> {
            log.info("server sent event timed out : id={}", id);
            //onCompletion 핸들러 호출
            emitter.complete();
        });

        //에러 핸들러 등록
        emitter.onError(e -> {
            log.info("server sent event error occurred : id={}, message={}", id, e.getMessage());
            //onCompletion 핸들러 호출
            emitter.complete();
        });

        //SSE complete 핸들러 등록
        emitter.onCompletion(() -> {
            if (emitterMap.remove(id) != null) {
                log.info("server sent event removed in emitter cache: id={}", id);
            }

            log.info("disconnected by completed server sent event: id={}", id);
        });

        emitterMap.put(id, emitter);

        //초기 연결시에 응답 데이터를 전송할 수도 있다.
        try {
            SseEmitter.SseEventBuilder event = SseEmitter.event()
                    //event 명 (event: event example)
                    .name("event example")
                    //event id (id: id-1) - 재연결시 클라이언트에서 `Last-Event-ID` 헤더에 마지막 event id 를 설정
                    .id(String.valueOf("id-1"))
                    //event data payload (data: SSE connected)
                    .data("SSE connected")
                    //SSE 연결이 끊어진 경우 재접속 하기까지 대기 시간 (retry: <RECONNECTION_TIMEOUT>)
                    .reconnectTime(RECONNECTION_TIMEOUT);
            emitter.send(event);
        } catch (IOException e) {
            log.error("failure send media position data, id={}, {}", id, e.getMessage());
        }
        return emitter;
    }

    public void broadcast(NotificationPayload notificationPayload) {
        emitterMap.forEach((id, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("broadcast event")
                        .id("broadcast event 1")
                        .reconnectTime(RECONNECTION_TIMEOUT)
                        .data(notificationPayload, MediaType.APPLICATION_JSON));
                log.info("sended notification, id={}, payload={}", id, notificationPayload);
            } catch (IOException e) {
                //SSE 세션이 이미 해제된 경우
                log.error("fail to send emitter id={}, {}", id, e.getMessage());
            }
        });
    }

    private SseEmitter createEmitter() {
        return new SseEmitter(TIMEOUT);
    }
}
