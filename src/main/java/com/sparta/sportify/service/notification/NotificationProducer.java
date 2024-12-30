package com.sparta.sportify.service.notification;

import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.repository.MatchRepository;
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

    @Scheduled(fixedRate = 60000)
    public void sendMatchNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<Match> upcomingMatches = matchRepository.findMatchesByStartTimeBetween(now, oneHourLater);

        for (Match match : upcomingMatches) {
            String notificationMessage = String.format(
                    "경기 알림: %s 경기장이 %s에 시작합니다!",
                    match.getStadiumTime().getStadium().getStadiumName(),
                    match.getStartTime()
            );

            Long userId = match.getStadiumTime().getStadium().getUser().getId();

            if (userId != null) {
                String messageToSend = String.format("{\"userId\": %d, \"message\": \"%s\"}", userId, notificationMessage);

                kafkaTemplate.send("match-notifications", messageToSend);
            } else {
                log.error("No userId found for stadium in notification message: {}", notificationMessage);
            }
        }
    }
}
