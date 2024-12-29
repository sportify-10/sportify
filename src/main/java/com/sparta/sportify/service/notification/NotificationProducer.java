package com.sparta.sportify.service.notification;

import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.notification.Notification;
import com.sparta.sportify.repository.MatchRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MatchRepository matchRepository;
    private final NotificationRepository notificationRepository; // Notification Repository 추가
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void sendMatchNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        // 1시간 이내에 시작하는 경기를 조회
        List<Match> upcomingMatches = matchRepository.findMatchesByStartTimeBetween(now, oneHourLater);

        for (Match match : upcomingMatches) {
            // 경기 알림 메시지 생성
            String notificationMessage = String.format(
                    "경기 알림: %s 경기장이 %s에 시작합니다!",
                    match.getStadiumTime().getStadium().getStadiumName(),
                    match.getStartTime().toString()
            );

            // 알림을 저장할 Notification 객체 생성
            Notification notification = new Notification();
            notification.setType("MATCH");
            notification.setStatus(Notification.NotificationStatus.PENDING);
            notification.setDeliveryMethod("PUSH");
            notification.setMessage(notificationMessage);
            notification.setCreatedAt(LocalDateTime.now());

            // 알림을 받을 사용자 정보 가져오기 (이 예시에서는 userId가 Notification에 저장되어 있다고 가정)
            Long userId = notification.getUserId(); // Notification 엔티티에 저장된 userId

            if (userId != null) {
                // userId가 존재하면 해당 사용자에게 알림을 전송
                String messageToSend = String.format("{\"userId\": %d, \"message\": \"%s\"}", userId, notificationMessage);

                // Kafka로 메시지 전송
                kafkaTemplate.send("match-notifications", messageToSend);
                // 알림을 데이터베이스에 저장
                notificationRepository.save(notification);
            } else {
                // userId가 없는 경우 로그에 기록
                log.error("No userId found for notification message: {}", notificationMessage);
            }
        }
    }
}
