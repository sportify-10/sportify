package com.sparta.sportify.service.notification;

import com.sparta.sportify.entity.match.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.sparta.sportify.repository.MatchRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchScheduler {

    private final MatchRepository matchRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000) // 1분마다 실행 (테스트용)
    public void sendMatchNotification() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        LocalDate date = oneHourLater.toLocalDate();
        LocalDateTime startRange = oneHourLater.minusMinutes(1);
        LocalDateTime endRange = oneHourLater.plusMinutes(1);

        // 시간 범위 내 경기 찾기
        List<Match> matches = matchRepository.findMatchesByStartTimeBetween(startRange, endRange);

        for (Match match : matches) {
            String message = "경기 '" + match.getName() + "'가 1시간 후 시작됩니다.";
            notificationService.sendMatchNotification(match.getId().toString(), message);
        }
    }
}
