package com.sparta.sportify.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.sportify.dto.match.MatchDetailResponseDto;
import com.sparta.sportify.dto.match.MatchResultRequestDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.dto.match.response.MatchByStadiumResponseDto;
import com.sparta.sportify.dto.match.response.MatchesByDateResponseDto;
import com.sparta.sportify.entity.Match;
import com.sparta.sportify.entity.MatchResult;
import com.sparta.sportify.entity.StadiumTime;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.MatchResultRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

	private final MatchResultRepository matchResultRepository;
	private final StadiumTimeRepository stadiumTimeRepository;
	private final MatchRepository matchRepository;

	@Transactional
	public MatchResultResponseDto createMatchResult(MatchResultRequestDto requestDto) {
		Match match = matchRepository.findById(requestDto.getMatchId())
			.orElseThrow(() -> new EntityNotFoundException("경기를 찾을 수 없습니다."));

		MatchResult matchResult = new MatchResult();
		matchResult.setTeamAScore(requestDto.getTeamAScore());
		matchResult.setTeamBScore(requestDto.getTeamBScore());
		matchResult.setMatch(match);
		matchResult.setMatchStatus(requestDto.getMatchStatus());
		matchResult.setMatchDate(LocalDate.now());


		MatchResult savedResult = matchResultRepository.save(matchResult);
		return new MatchResultResponseDto(
			savedResult.getId(),
			savedResult.getTeamAScore(),
			savedResult.getTeamBScore(),
			savedResult.getMatchStatus(),
			savedResult.getMatchDate()
		);
	}

	@Transactional(readOnly = true)
	public MatchResultResponseDto getMatchResult(Long matchId) {
		MatchResult matchResult = matchResultRepository.findByMatchId(matchId)
			.orElseThrow(() -> new EntityNotFoundException("경기 결과를 찾을 수 없습니다."));

		return new MatchResultResponseDto(
			matchResult.getId(),
			matchResult.getTeamAScore(),
			matchResult.getTeamBScore(),
			matchResult.getMatchStatus(),
			matchResult.getMatchDate()
		);
  }

	public MatchesByDateResponseDto getMatchesByDate(LocalDate date/*int page, int size, LocalDate date, UserDetailsImpl userDetails*/) {
		//Pageable pageable = PageRequest.of(page, size);

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

		//요일 구하고 MON, TUE 대문자 형태로 변환
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		String cronDay = dayOfWeek.toString().substring(0, 3).toUpperCase();

		//FULLTEXT 검색
		List<StadiumTime> stadiumTimes = stadiumTimeRepository.findByCronDay(cronDay);
		//저장된 스타디움 타임이 없으면
		if (stadiumTimes.isEmpty()) {
			return new MatchesByDateResponseDto(List.of());
		}
		for (int i = 0; i < stadiumTimes.size(); i++) {
			String cron = stadiumTimes.get(i).getCron();//스타디움 타임에 저장된 크론식 조회

			//크론식에 해당 요일이 없으면 건너뛰기
			if(!cron.contains(cronDay)){
				continue;
			}

			//크론식에서 시간 추출
			List<Integer> startTimeList = extractStartTimes(cron, cronDay);

			for (int j = 0; j < startTimeList.size(); j++) {
				int startTime = startTimeList.get(j);
				int endTime = startTime + 2;

				//크론식의 시작시간과 종료 시간
				String startTimeFormatted = String.format("%02d:00", startTime);
				String endTimeFormatted = String.format("%02d:00", endTime);

				LocalTime startTimeLocalTimeType = LocalTime.parse(startTimeFormatted); //LocalTime 형식으로 변환
				LocalDateTime startDateTime = date.atTime(startTimeLocalTimeType);//입력한 날짜 시간
				Duration duration = Duration.between(LocalDateTime.now(), startDateTime);//시간 비교

				int startTimeInt = startTimeLocalTimeType.getHour() * 100;//Integer형식과 비교하기 위해 변환
				//매치 테이블에서 예약 인원 수 조회하기 위헤
				Optional<Match> match = matchRepository.findByStadiumTimeIdAndDateAndTime(stadiumTimes.get(i).getId(),date, startTimeInt);

				String status = ""; //마감, 모집중, 마감 임박

				double totalMatchCount = 0;//매치에 예약된 인원 수
				double totalStadiumCapacity = 0;//구장애서 정한 최대 인원 수
				double reservationPercentage = 0;
				if (match.isPresent()) {
					//예약 인원 %
					totalMatchCount = match.get().getATeamCount() + match.get().getBTeamCount();
					totalStadiumCapacity = stadiumTimes.get(i).getStadium().getATeamCount() + stadiumTimes.get(i).getStadium().getBTeamCount();
					reservationPercentage = (totalMatchCount / totalStadiumCapacity) * 100;
				}

				//시간이 지난 경우 || 인원이 다 찬 경우
				if(duration.isNegative() || reservationPercentage == 100) {
					status = "마감";
				}
				//현재 시작시간 까지 4시간 이내로 남은 경우 || 인원 80% 이상인 경우
				else if(duration.toHours() <= 4 || reservationPercentage > 80) {
					status = "마감 임박";
				}

				else {
					status = "모집중";
				}

				MatchByStadiumResponseDto matchResponse = new MatchByStadiumResponseDto(
					stadiumTimes.get(i).getStadium().getId(),
					stadiumTimes.get(i).getStadium().getStadiumName(),
					stadiumTimes.get(i).getStadium().getDescription(),
					stadiumTimes.get(i).getStadium().getLocation(),
					startTimeFormatted,
					endTimeFormatted,
					status
				);

				matches.add(matchResponse);
			}
		}
		matches.sort(Comparator.comparing(MatchByStadiumResponseDto::getStartTime));

		return new MatchesByDateResponseDto(matches);
	}

	//시작 시간 추출 메서드
	private List<Integer> extractStartTimes(String cronExpr, String cronDay) {
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

	// 매치 단건 조회
	@Transactional(readOnly = true)
	public MatchDetailResponseDto getMatchByDateAndTime(LocalDate date, String time) {
		// 매치 조회
		Match match = matchRepository.findByDateAndTime(date, time)
			.orElseThrow(() -> new EntityNotFoundException("해당 날짜와 시간에 매치가 존재하지 않습니다."));

		// 매치 상태 결정
		String status = determineMatchStatus(match);

		return new MatchDetailResponseDto(
			match.getId(),
			match.getDate(),
			match.getTime(),
			match.getATeamCount(),
			match.getBTeamCount(),
			match.getStadiumTime().getStadium().getStadiumName(),
			status
		);
	}
	// 매치 상태 결정
	private String determineMatchStatus(Match match) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime matchStartTime = LocalDateTime.of(match.getDate(), LocalTime.parse(match.getTime()));
		LocalDateTime matchEndTime = matchStartTime.plusHours(2); // 종료 시간은 시작 시간 + 2시간

		// 예약 인원 수 계산
		double totalMatchCount = match.getATeamCount() + match.getBTeamCount();
		double totalStadiumCapacity = match.getStadiumTime().getStadium().getATeamCount() + match.getStadiumTime().getStadium().getBTeamCount();
		double reservationPercentage = (totalMatchCount / totalStadiumCapacity) * 100;

		// 상태 결정 로직
		if (now.isAfter(matchEndTime)) {
			return "마감"; // 종료 시간이 지난 경우
		} else if (now.isAfter(matchStartTime.minusHours(4)) || reservationPercentage > 80) {
			return "마감 임박"; // 시작 4시간 이내이거나 예약 비율이 80% 이상인 경우
		} else {
			return "모집중"; // 그 외의 경우
		}
	}
}