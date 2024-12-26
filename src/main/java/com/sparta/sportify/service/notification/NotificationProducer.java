package com.sparta.sportify.service.notification;

import com.sparta.sportify.entity.Match;
import com.sparta.sportify.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MatchRepository matchRepository;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void sendMatchNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        // 1시간 이내에 시작하는 경기를 조회
        List<Match> upcomingMatches = matchRepository.findMatchesByStartTimeBetween(now, oneHourLater);

        for (Match match : upcomingMatches) {
            String notificationMessage = String.format(
                    "경기 알림: %s 경기장이 %s에 시작합니다!",
                    match.getStadiumTime().getStadium().getStadiumName(),
                    match.getStartTime().toString()
            );
            kafkaTemplate.send("match-notifications", notificationMessage);
        }
    }
}
