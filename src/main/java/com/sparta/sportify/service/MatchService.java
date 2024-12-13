package com.sparta.sportify.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.sportify.dto.match.MatchDetailResponseDto;
import com.sparta.sportify.dto.match.MatchResultRequestDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.dto.match.response.MatchByStadiumResponseDto;
import com.sparta.sportify.dto.match.response.MatchesByDateResponseDto;
import com.sparta.sportify.entity.Match;
import com.sparta.sportify.entity.MatchResult;
import com.sparta.sportify.entity.Reservation;
import com.sparta.sportify.entity.StadiumTime;
import com.sparta.sportify.entity.Team;
import com.sparta.sportify.entity.TeamColor;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.MatchResultRepository;
import com.sparta.sportify.repository.ReservationRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MatchService {

	private final MatchResultRepository matchResultRepository;
	private final StadiumTimeRepository stadiumTimeRepository;
	private final MatchRepository matchRepository;
	private final ReservationRepository reservationRepository;
	private final UserRepository userRepository;
	private final TeamRepository teamRepository;

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

		// 경기 결과에 따른 개인 점수 부여
		List<Reservation> reservations = reservationRepository.findAllByMatch(match);
		reservations.forEach(reservation -> {
			User user = reservation.getUser();
			int pointChange = 0;

			if (requestDto.getTeamAScore() > requestDto.getTeamBScore()) {
				pointChange = (reservation.getTeamColor() == TeamColor.A) ? 10 : -10;
			} else if (requestDto.getTeamBScore() > requestDto.getTeamAScore()) {
				pointChange = (reservation.getTeamColor() == TeamColor.B) ? 10 : -10;
			} else {
				pointChange = 5;
			}

			user.setLevelPoints(user.getLevelPoints() + pointChange);
			userRepository.save(user);
		});

		// 경기 결과에 따른 팀 점수 부여
		List<Reservation> reservationsTeam = reservationRepository.findAllByMatch(match);
		// 중복 없는 Team-Reservation 매핑을 Set으로 생성
		Set<Map<Team, Reservation>> uniqueTeamReservations = new HashSet<>();
		reservationsTeam.forEach(reservation -> {
			Team team = reservation.getTeam();
			if (team != null) {
				Map<Team, Reservation> map = new HashMap<>();
				map.put(team, reservation);
				uniqueTeamReservations.add(map);
			}
		});
		// Set 처리
		Set<Team> uniqueTeams = reservationsTeam.stream()
			.map(Reservation::getTeam)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		uniqueTeams.forEach(team -> {
			// 관련된 Reservation 중 하나를 가져오기 (팀 기반)
			Reservation relatedReservation = reservationsTeam.stream()
				.filter(reservation -> team.equals(reservation.getTeam()))
				.findFirst() // 첫 번째 매칭된 Reservation 가져오기
				.orElseThrow(() -> new IllegalStateException("Reservation not found for team"));
			// Reservation의 TeamColor로 점수 계산
			TeamColor teamColor = relatedReservation.getTeamColor();
			int teampointChange = 0;
			if (requestDto.getTeamAScore() > requestDto.getTeamBScore()) {
				teampointChange = (teamColor == TeamColor.A) ? 10 : -10;
			} else if (requestDto.getTeamBScore() > requestDto.getTeamAScore()) {
				teampointChange = (teamColor == TeamColor.B) ? 10 : -10;
			} else {
				teampointChange = 5;
			}
			// 점수 업데이트
			team.setTeamPoints(team.getTeamPoints() + teampointChange);
			teamRepository.save(team);
		});


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

	public MatchesByDateResponseDto getMatchesByDate(LocalDate date) {
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
			return new MatchesByDateResponseDto(matches);
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

				int startTimeInt = startTimeLocalTimeType.getHour();//Integer형식과 비교하기 위해 변환
				//매치 테이블에서 예약 인원 수 조회하기 위헤
				Optional<Match> match = matchRepository.findByStadiumTimeIdAndDateAndTime(stadiumTimes.get(i).getId(),date, startTimeInt);

				if(match.isEmpty()){
					continue;
				}

				//마감, 모집중, 마감 임박
				String status = determineMatchStatus(match, LocalDateTime.now());

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
	public MatchDetailResponseDto getMatchByDateAndTime(Long stadiumId, LocalDate date, Integer time, LocalDateTime now) {
		// 매치 조회
		StadiumTime stadiumTime = stadiumTimeRepository.findByStadiumId(stadiumId)
			.orElseThrow(() -> new EntityNotFoundException("해당 경기장에 대한 경기 시간 정보를 찾을 수 없습니다."));
		Match match = matchRepository.findByStadiumTimeIdAndDateAndTime(stadiumTime.getId(), date, time)
			.orElseThrow(() ->new EntityNotFoundException("해당 날짜와 시간에 매치가 존재하지 않습니다."));

		// 매치 상태 결정
		String status = determineMatchStatus(Optional.ofNullable(match), now);
		//String status = determineMatchStatus(match.getStartTime(), match.getEndTime(), match.getReservationPercentage(), now);

		return new MatchDetailResponseDto(
			match.getId(),
			match.getDate(),
			String.format("%02d:%02d", match.getTime(), 0),
			match.getATeamCount(),
			match.getBTeamCount(),
			match.getStadiumTime().getStadium().getStadiumName(),
			status
		);
	}

	//매치 상태 결정
	private String determineMatchStatus(Optional<Match> match, LocalDateTime now) {
		LocalDateTime matchStartTime = match.get().getStartTime();
		LocalDateTime matchEndTime = match.get().getEndTime();

		// 예약 인원 수 계산
		double reservationPercentage = match.get().getReservationPercentage();

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