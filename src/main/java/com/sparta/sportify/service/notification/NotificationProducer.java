package com.sparta.sportify.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MatchRepository matchRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.topic}")
    private String kafkaTopic;

    @Value("${spring.kafka.timezone}")
    private String timezone;

    @Scheduled(fixedRateString = "${spring.kafka.schedule.rate}")
    public void sendMatchNotifications() {
        try {
            LocalDateTime now = LocalDateTime.now(ZoneId.of(timezone));
            LocalDateTime oneHourLater = now.plusHours(1);

            List<Match> upcomingMatches = matchRepository.findMatchesByStartTimeBetween(now, oneHourLater);

            for (Match match : upcomingMatches) {
                sendMatchNotification(match);
            }
        } catch (Exception e) {
            log.error("경기 알림 처리 중 오류 발생", e);
        }
    }

    private void sendMatchNotification(Match match) {
        try {
            Long userId = match.getStadiumTime().getStadium().getUser().getId();
            if (userId == null) {
                log.error("경기장에 대한 사용자 ID를 찾을 수 없음: {}",
                        match.getStadiumTime().getStadium().getStadiumName());
                return;
            }

            Map<String, Object> payload = Map.of(
                    "userId", userId,
                    "message", createNotificationMessage(match)
            );

            String message = objectMapper.writeValueAsString(payload);

            kafkaTemplate.send(kafkaTopic, message)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("매치 ID {} 알림 전송 실패", match.getId(), ex);
                        } else {
                            log.debug("매치 ID {} 알림 전송 성공", match.getId());
                        }
                    });

        } catch (Exception e) {
            log.error("매치 ID {} 알림 처리 중 오류", match.getId(), e);
        }
    }

    private String createNotificationMessage(Match match) {
        return String.format(
                "경기 알림: %s 경기장이 %s에 시작합니다!",
                match.getStadiumTime().getStadium().getStadiumName(),
                match.getStartTime()
        );
    }
}