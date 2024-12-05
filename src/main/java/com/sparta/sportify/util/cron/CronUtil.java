package com.sparta.sportify.util.cron;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class CronUtil {

    public boolean isCronDateAllowed(String cronExpression, LocalDate date, int hour) {
        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
        Cron cron = parser.parse(cronExpression);

        ExecutionTime executionTime = ExecutionTime.forCron(cron);

        LocalDateTime dateTime = date.atTime(hour, 0);

        // 크론식 실행 가능 여부 확인
        Optional<Boolean> isMatch = Optional.of(executionTime.isMatch(dateTime.atZone(ZoneId.systemDefault())));
        return isMatch.orElse(false);
    }

//    날짜별 매치 리스트 구현하기 위한 레퍼런스
//    public List<String> sortStoreSchedules(List<Map<String, String>> scheduleData) {
//        CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(UNIX));
//
//        List<String> scheduleList = new ArrayList<>();
//        for (Map<String, String> data : scheduleData) {
//            String cronExpr = data.get("cronExpr");
//            String storeName = data.get("storeName");
//
//            // Cron 파싱
//            Cron cron = parser.parse(cronExpr);
//            Set<Integer> hours = cron.retrieve("hours").getValues(); // 시간
//            Set<Integer> daysOfWeek = cron.retrieve("dayOfWeek").getValues(); // 요일
//
//            // 요일 및 시간 데이터를 읽어서 결과 생성
//            for (Integer day : daysOfWeek) {
//                for (Integer hour : hours) {
//                    scheduleList.add(String.format("%02d시 %s - %s", hour, DAY_OF_WEEK_MAP.get(day), storeName));
//                }
//            }
//        }
//
//        // 시간(시) -> 가게 이름 순서로 정렬
//        scheduleList.sort(Comparator.comparing(s -> {
//            String[] parts = s.split(" "); // "09시 월요일 - A"
//            int hour = Integer.parseInt(parts[0].replace("시", "")); // 시간
//            String storeName = parts[3]; // 가게 이름
//            return hour * 1000 + storeName.hashCode(); // 시간 우선, 같은 시간대는 가게 이름으로 정렬
//        }));
//
//        return scheduleList;
//    }

}
