package com.sparta.sportify.util.cron;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.sparta.sportify.dto.stadiumTime.request.StadiumTimeRequestDto;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class CronUtil {

	public static boolean isCronDateAllowed(String cronExpression, LocalDate date, int hour) {
		CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
		Cron cron = parser.parse(cronExpression);

		ExecutionTime executionTime = ExecutionTime.forCron(cron);

		LocalDateTime dateTime = date.atTime(hour, 0);

		// 크론식 실행 가능 여부 확인
		Optional<Boolean> isMatch = Optional.of(executionTime.isMatch(dateTime.atZone(ZoneId.systemDefault())));
		return isMatch.orElse(false);
	}

	public static List<Integer> extractStartTimes(String cronExpr) {
		String[] parts = cronExpr.split(" ");
		String timeRange = parts[2];  // 시간 정보는 cron의 세 번째 부분에 위치

		List<Integer> startTimes = new ArrayList<>();
		//모든 시간인 경우
		if ("*".equals(timeRange)) {
			for (int i = 0; i <= 22; i++) {
				startTimes.add(i);
			}
			return startTimes;
		}

		//"08-12,13-15,15-17"에서 ,기준으로 나누기
		String[] timeRanges = timeRange.split(",");

		for (int i = 0; i < timeRanges.length; i++) {
			String range = timeRanges[i];
			String[] hours = range.split("-");

			int start = Integer.parseInt(hours[0]);
			int end = Integer.parseInt(hours[1]);

			// 2시간씩 증가하면서 추가
			for (int j = start; j < end; j += 2) {
				startTimes.add(j);
			}
		}

		return startTimes;
	}

	public static String convertToCronExpression(List<String> weeks, List<String> hours) {

		//크론식 요일 항상 대문자로 저장
		weeks = weeks.stream()
			.map(String::toUpperCase)
			.collect(Collectors.toList());
		StringBuilder cronBuilder = new StringBuilder();

		for (int i = 0; i < hours.size(); i++) {
			String[] hour = hours.get(i).split("-");
			String startHour = hour[0];
			String endHour = hour[1];

			if (i > 0) {
				cronBuilder.append(",");
			}

			cronBuilder.append(startHour).append("-").append(endHour);
		}

		String hourString = cronBuilder.toString();

		String weekString = "";
		for (int i = 0; i < weeks.size(); i++) {
			weekString += weeks.get(i);
			if (i < weeks.size() - 1) { //마지막이면 쉼표 추가x
				weekString += ",";
			}
		}

		return String.format("0 0 %s ? * %s", hourString, weekString);
	}
}
