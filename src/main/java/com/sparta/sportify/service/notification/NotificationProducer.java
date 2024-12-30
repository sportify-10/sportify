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
    private final NotificationRepository notificationRepository;

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

            // Stadium에서 userId 가져오기
            Long userId = match.getStadiumTime().getStadium().getUser().getId(); // Stadium 엔티티의 userId

            if (userId != null) {
                // 알림을 저장할 Notification 객체 생성
                Notification notification = new Notification();
                notification.setType("MATCH");
                notification.setStatus(Notification.NotificationStatus.PENDING);
                notification.setDeliveryMethod("PUSH");
                notification.setMessage(notificationMessage);
                notification.setCreatedAt(LocalDateTime.now());
                notification.setUserId(userId); // userId 설정

                // Kafka 메시지 생성
                String messageToSend = String.format("{\"userId\": %d, \"message\": \"%s\"}", userId, notificationMessage);

                // Kafka로 메시지 전송
                kafkaTemplate.send("match-notifications", messageToSend);

                // 알림을 데이터베이스에 저장
                notificationRepository.save(notification);
            } else {
                // userId가 없는 경우 로그에 기록
                log.error("No userId found for stadium in notification message: {}", notificationMessage);
            }
        }
    }
}
