package com.sparta.sportify.service.match;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sparta.sportify.dto.match.MatchDetailResponseDto;
import com.sparta.sportify.dto.match.MatchResultRequestDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.dto.match.response.MatchByStadiumResponseDto;
import com.sparta.sportify.dto.match.response.MatchesByDateResponseDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.matchResult.MatchResult;
import com.sparta.sportify.entity.matchResult.MatchStatus;
import com.sparta.sportify.entity.reservation.Reservation;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.team.TeamColor;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.MatchResultRepository;
import com.sparta.sportify.repository.ReservationRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.service.MatchService;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {
	@Mock
	private MatchRepository matchRepository;

	@Mock
	private MatchResultRepository matchResultRepository;

	@Mock
	private ReservationRepository reservationRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TeamRepository teamRepository;

	@Mock
	private StadiumTimeRepository stadiumTimeRepository;

	@InjectMocks
	private MatchService matchService;

	private Match match;
	private MatchResultRequestDto requestDto;
	private MatchResult matchResult;
	private StadiumTime stadiumTime;
	private Stadium stadium;

	@BeforeEach
	void setUp() {
		stadium = new Stadium(
			1L,
			"National Stadium",
			"Seoul, Korea",
			6,
			6,
			"A large stadium for concerts.",
			50000L,
			StadiumStatus.APPROVED,
			null,
			null
		);
		stadiumTime = StadiumTime.builder()
			.id(1L)
			.stadium(stadium)
			.cron("MON,TUE. WED")
			.build();
		match = Match.builder()
			.id(1L)
			.date(LocalDate.of(2024, 12, 27))
			.time(15)
			.aTeamCount(11)
			.bTeamCount(11)
			.stadiumTime(stadiumTime)
			.build(); // 매치 초기화
		requestDto = new MatchResultRequestDto(5, 10, MatchStatus.CLOSED, 1L); // 요청 DTO 초기화

		matchResult = MatchResult.builder()
			.id(1L)
			.teamAScore(10)
			.teamBScore(5)
			.matchStatus(MatchStatus.CLOSED)
			.matchDate(LocalDate.now())
			.build();
	}

	@Test
	@DisplayName("매치 조회 성공")
	void findMatch_Success() {
		// Given
		when(matchRepository.findById(requestDto.getMatchId())).thenReturn(Optional.of(match));

		// When
		Match result = matchRepository.findById(requestDto.getMatchId())
			.orElseThrow(() -> new CustomApiException(ErrorCode.MATCH_NOT_FOUND));

		// Then
		assertNotNull(result);
		assertEquals(match.getId(), result.getId());
		verify(matchRepository, times(1)).findById(requestDto.getMatchId());
	}

	@Test
	@DisplayName("매치 조회 실패")
	void findMatch_NotFound() {
		// Given
		when(matchRepository.findById(requestDto.getMatchId())).thenReturn(Optional.empty());

		// When & Then
		CustomApiException exception = assertThrows(CustomApiException.class, () -> {
			matchRepository.findById(requestDto.getMatchId())
				.orElseThrow(() -> new CustomApiException(ErrorCode.MATCH_NOT_FOUND));
		});

		assertEquals(ErrorCode.MATCH_NOT_FOUND, exception.getErrorCode());
		verify(matchRepository, times(1)).findById(requestDto.getMatchId());
	}

	@Test
	@DisplayName("점수 추가 로직 테스트")
	void createMatchResult_SavesMatchResultAndUpdatesPoints() {
		// Given
		when(matchRepository.findById(requestDto.getMatchId())).thenReturn(Optional.of(match));

		MatchResult matchResult = MatchResult.builder()
			.teamAScore(requestDto.getTeamAScore())
			.teamBScore(requestDto.getTeamBScore())
			.match(match)
			.matchStatus(requestDto.getMatchStatus())
			.matchDate(LocalDate.now())
			.build();

		when(matchResultRepository.save(any(MatchResult.class))).thenReturn(matchResult);

		User userA = User.builder()
			.id(1L)
			.build();
		User userB = User.builder()
			.id(2L)
			.build();
		Team teamA = Team.builder()
			.id(1L)
			.teamName("TeamA")
			.teamPoints(1000)
			.build();
		Team teamB = Team.builder()
			.id(2L)
			.teamName("TeamB")
			.teamPoints(1000)
			.build();
		Reservation reservationA = Reservation.builder()
			.user(userA)
			.team(teamA)
			.match(match)
			.teamColor(TeamColor.A)
			.build();
		Reservation reservationB = Reservation.builder()
			.user(userB)
			.team(teamB)
			.match(match)
			.teamColor(TeamColor.B)
			.build();

		List<Reservation> reservations = new ArrayList<>();
		reservations.add(reservationA);
		reservations.add(reservationB);

		when(reservationRepository.findAllByMatch(match)).thenReturn(reservations);

		// When
		MatchResultResponseDto savedResult = matchService.createMatchResult(requestDto);

		// Then
		assertNotNull(savedResult);
		assertEquals(matchResult.getId(), savedResult.getId());
		assertEquals(matchResult.getTeamAScore(), savedResult.getTeamAScore());
		assertEquals(matchResult.getTeamBScore(), savedResult.getTeamBScore());
		assertEquals(matchResult.getMatchStatus(), savedResult.getMatchStatus());
		assertEquals(matchResult.getMatchDate(), savedResult.getMatchDate());

		// User points verification
		verify(userRepository, times(1)).save(userA); // userA의 점수 업데이트
		verify(userRepository, times(1)).save(userB); // userB의 점수 업데이트
		assertEquals(990, userA.getLevelPoints()); // userA는 10점 증가
		assertEquals(1010, userB.getLevelPoints()); // userB는 -10점 감소

		// Team points verification
		//verify(teamRepository, times(1)).save(any(Team.class)); // 팀 점수 업데이트
		assertEquals(990, teamA.getTeamPoints()); // Team A는 10점 증가
		assertEquals(1010, teamB.getTeamPoints()); // Team B는 -10점 감소
	}

	@Test
	@DisplayName("매치 결과값 조회 성공")
	void getMatchResult_ReturnsMatchResultDto_WhenMatchFound() {
		// Given
		Long matchId = 1L;
		when(matchResultRepository.findByMatchId(matchId)).thenReturn(Optional.ofNullable(matchResult));

		// When
		MatchResultResponseDto response = matchService.getMatchResult(matchId);

		// Then
		assertNotNull(response);
		assertEquals(matchResult.getId(), response.getId());
		assertEquals(matchResult.getTeamAScore(), response.getTeamAScore());
		assertEquals(matchResult.getTeamBScore(), response.getTeamBScore());
		assertEquals(matchResult.getMatchStatus(), response.getMatchStatus());
		assertEquals(matchResult.getMatchDate(), response.getMatchDate());
	}

	@Test
	@DisplayName("매치 결과가 없을 때 CustomApiException 발생")
	void getMatchResult_ThrowsCustomApiException_WhenMatchNotFound() {
		// Given
		Long matchId = 1L;
		when(matchResultRepository.findByMatchId(matchId)).thenReturn(Optional.empty());

		// When & Then
		CustomApiException exception = assertThrows(CustomApiException.class, () -> {
			matchService.getMatchResult(matchId);
		});

		assertEquals(ErrorCode.MATCHRESULT_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("매치 단건조회 성공")
	void getMatchByDateAndTime_ReturnsMatchDetailResponseDto_WhenMatchFound() {
		// Given
		Long stadiumId = 1L;
		LocalDate date = LocalDate.of(2024, 12, 27);
		Integer time = 15;
		LocalDateTime now = LocalDateTime.now().minusHours(1); // 현재 시간이 시작 1시간 전

		when(stadiumTimeRepository.findByStadiumId(stadiumId)).thenReturn(Optional.of(stadiumTime));
		when(matchRepository.findByStadiumTimeIdAndDateAndTime(any(), any(), any())).thenReturn(
			Optional.of(match));

		// When
		MatchDetailResponseDto response = matchService.getMatchByDateAndTime(stadiumId, date, time, now);

		// Then
		assertNotNull(response, "Response should not be null");
		assertEquals(match.getId(), response.getMatchId(), "Match ID should match");
		assertEquals(match.getDate(), response.getDate(), "Match date should match");
		assertEquals(String.format("%02d:%02d", match.getTime(), 0), response.getTime(), "Match time should match");
		assertEquals(match.getATeamCount(), response.getATeamCount(), "A Team count should match");
		assertEquals(match.getBTeamCount(), response.getBTeamCount(), "B Team count should match");
		assertEquals(stadiumTime.getStadium().getStadiumName(), response.getStadiumName(), "Stadium name should match");
		assertEquals(MatchStatus.CLOSED, response.getStatus(), "Match status should be OPEN"); // 상태 검증
	}

	@Test
	@DisplayName("스터디움 타임을 찾지 못해 매치 단건조회 실패")
	void getMatchByDateAndTime_ThrowsCustomApiException_WhenStadiumTimeNotFound() {
		// Given
		Long stadiumId = 1L;
		LocalDate date = LocalDate.of(2024, 12, 27);
		Integer time = 15;
		LocalDateTime now = LocalDateTime.now();

		when(stadiumTimeRepository.findByStadiumId(stadiumId)).thenReturn(Optional.empty());

		// When & Then
		CustomApiException exception = assertThrows(CustomApiException.class, () -> {
			matchService.getMatchByDateAndTime(stadiumId, date, time, now);
		});

		assertEquals(ErrorCode.STADIUMTIME_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("매치를 찾지 못해 매치 단건조회 실패")
	void getMatchByDateAndTime_ThrowsCustomApiException_WhenMatchNotFound() {
		// Given
		Long stadiumId = 1L;
		LocalDate date = LocalDate.of(2024, 12, 27);
		Integer time = 15;
		LocalDateTime now = LocalDateTime.now();

		when(stadiumTimeRepository.findByStadiumId(stadiumId)).thenReturn(Optional.of(stadiumTime));
		when(matchRepository.findByStadiumTimeIdAndDateAndTime(stadiumTime.getId(), date, time)).thenReturn(
			Optional.empty());

		// When & Then
		CustomApiException exception = assertThrows(CustomApiException.class, () -> {
			matchService.getMatchByDateAndTime(stadiumId, date, time, now);
		});

		assertEquals(ErrorCode.MATCH_NOT_FOUND, exception.getErrorCode());
	}

	@Test
	@DisplayName("매치가 종료되서 매치 단건조회 실패")
	void determineMatchStatus_ReturnsClosed_WhenMatchEnded() {
		// Given
		LocalDateTime now = LocalDateTime.of(2024, 12, 27, 18, 0); // 현재 시간이 종료 후
		LocalDateTime endTime = now.minusMinutes(1); // 종료 시간 설정

		// Match 객체 생성
		match = Match.builder()
			.id(1L)
			.date(LocalDate.of(2024, 12, 27))
			.time(14)
			.aTeamCount(11)
			.bTeamCount(11)
			.stadiumTime(stadiumTime)
			.build();

		// When
		MatchStatus status = matchService.determineMatchStatus(match, now);

		// Then
		assertEquals(MatchStatus.CLOSED, status);
	}
}