package com.sparta.sportify.service.notification;

import com.sparta.sportify.entity.match.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.sparta.sportify.repository.MatchRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MatchScheduler {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC_NAME = "match-notification";
    private final MatchRepository matchRepository;

    @Scheduled(fixedRate = 60000) // 1분마다 실행 (테스트용)
    public void sendMatchNotification() {
        LocalDateTime now = LocalDateTime.now();
        List<Match> matches = findMatchesStartingInOneHour(now);

        for (Match match : matches) {
            String message = "경기 '" + match.getName() + "'가 1시간 후 시작됩니다.";
            kafkaTemplate.send(TOPIC_NAME, match.getId().toString(), message);
        }
    }

    private List<Match> findMatchesStartingInOneHour(LocalDateTime now) {
        // 경기가 1시간 후에 시작하는 것을 필터링 (가상의 데이터 조회 메서드)
        return matchRepository.findMatchesStartingAt(now.plusHours(1));
    }
}
