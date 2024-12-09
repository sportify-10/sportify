package com.sparta.sportify.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.match.response.MatchByStadiumResponseDto;
import com.sparta.sportify.dto.match.response.MatchesByDateResponseDto;
import com.sparta.sportify.entity.StadiumTime;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

	private final StadiumTimeRepository stadiumTimeRepository;

	public MatchesByDateResponseDto getMatchesByDate(/*int page, int size, LocalDate date, UserDetailsImpl userDetails*/) {
		//Pageable pageable = PageRequest.of(page, size);
		List<StadiumTime> stadiumTimes = stadiumTimeRepository.findAll();

		//저장된 스타디움 타임이 없으면
		if (stadiumTimes.isEmpty()) {
			return new MatchesByDateResponseDto(List.of());
		}

		//하나의 값? 저장할 리스트
		List<MatchByStadiumResponseDto> matches = new ArrayList<>();
		/* 하나의 값? 예시
		"stadiumId": 2,
		"stadiumName": "A구장",
		"stadiumDescription": "넓고 좋은 경기장",
		"stadiumLocation": "서울",
		"startTime": "08:00",
		"endTime": "10:00"
		*/

		for (int i = 0; i < stadiumTimes.size(); i++) {
			String cron = stadiumTimes.get(i).getCron();//스타디움 타임에 저장된 크론식 조회
			List<Integer> startTimeList = extractStartTimes(cron);

			for (int j = 0; j < startTimeList.size(); j++) {
				int startTime = startTimeList.get(j);
				int endTime = startTime + 2;

				String startTimeFormatted = String.format("%02d:00", startTime);
				String endTimeFormatted = String.format("%02d:00", endTime);

				MatchByStadiumResponseDto matchResponse = new MatchByStadiumResponseDto(
					stadiumTimes.get(i).getStadium().getId(),
					stadiumTimes.get(i).getStadium().getStadiumName(),
					stadiumTimes.get(i).getStadium().getDescription(),
					stadiumTimes.get(i).getStadium().getLocation(),
					startTimeFormatted,
					endTimeFormatted
				);

				matches.add(matchResponse);
			}
		}
		return new MatchesByDateResponseDto(matches);
	}

	//시작 시간 추출 메서드
	private List<Integer> extractStartTimes(String cronExpr) {
		String[] parts = cronExpr.split(" ");
		String timeRange = parts[2];  // 시간 정보는 cron의 세 번째 부분에 위치

		//"08-12,13-15,15-17"에서 ,기준으로 나누기
		String[] timeRanges = timeRange.split(",");
		List<Integer> startTimes = new ArrayList<>();

		for (int i = 0; i < timeRanges.length; i++) {
			String range = timeRanges[i];
			String[] hours = range.split("-");

			int start = Integer.parseInt(hours[0]);  // -문자 앞에 있는 값
			startTimes.add(start);
		}

		return startTimes;
	}
}