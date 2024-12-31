package com.sparta.sportify.service.match;

import static com.sparta.sportify.entity.stadium.QStadium.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sparta.sportify.dto.match.MatchResultRequestDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.dto.match.response.MatchByStadiumResponseDto;
import com.sparta.sportify.dto.match.response.MatchesByDateResponseDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.matchResult.MatchResult;
import com.sparta.sportify.entity.matchResult.MatchStatus;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.MatchResultRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.MatchService;

import jakarta.persistence.EntityNotFoundException;

class MatchServiceTest {
	@InjectMocks
	private MatchService matchService;

	@Mock
	private MatchResultRepository matchResultRepository;

	@Mock
	private MatchRepository matchRepository;

	@Mock
	private StadiumTimeRepository stadiumTimeRepository;

	private User user;
	private UserDetailsImpl userDetails;
	private Stadium stadium;
	private StadiumTime stadiumTime;
	private Match match;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		user = User.builder()
			.id(1L)
			.active(true)
			.age(20L)
			.deletedAt(null)
			.email("test@example.com")
			.name("John Doe")
			.password("password123")
			.role(UserRole.USER)
			.cash(1000L)
			.build();
		userDetails = new UserDetailsImpl(user.getName(), user.getRole(), user);

		stadium = Stadium.builder()
			.id(1L)
			.stadiumName("A구장")
			.location("서울")
			.aTeamCount(5)
			.bTeamCount(5)
			.description("종아요~")
			.price(100000L)
			.status(StadiumStatus.APPROVED)
			.deletedAt(null)
			.user(user)
			.build();

		stadiumTime = StadiumTime.builder()
			.id(1L)
			.cron("0 0 08-10,10-12,20-22 ? * MON,TUE")
			.stadium(stadium)
			.build();

		match = Match.builder()
			.id(1L)
			.date(LocalDate.of(2024, 12, 31))
			.time(20)
			.aTeamCount(4)
			.bTeamCount(6)
			.stadiumTime(stadiumTime)
			.build();
	}

	@Test
	@DisplayName("createMatchResult 메소드의 정상 동작을 확인합니다.")
	public void createMatchResult_ShouldReturnMatchResultResponseDto() {
		// Given
		MatchResultRequestDto requestDto = new MatchResultRequestDto();
		requestDto.setMatchId(1L);
		requestDto.setTeamAScore(2);
		requestDto.setTeamBScore(3);
		requestDto.setMatchStatus(MatchStatus.CLOSED);

		Match match = new Match(); // Match 객체 생성 및 필요한 데이터 설정
		when(matchRepository.findById(requestDto.getMatchId())).thenReturn(Optional.of(match));

		MatchResult savedResult = new MatchResult();
		savedResult.setId(1L);
		savedResult.setTeamAScore(requestDto.getTeamAScore());
		savedResult.setTeamBScore(requestDto.getTeamBScore());
		savedResult.setMatchStatus(requestDto.getMatchStatus());
		savedResult.setMatchDate(LocalDate.now());

		when(matchResultRepository.save(any(MatchResult.class))).thenReturn(savedResult);

		// When
		MatchResultResponseDto responseDto = matchService.createMatchResult(requestDto);

		// Then
		assertNotNull(responseDto);
		assertEquals(1L, responseDto.getId());
		assertEquals(2, responseDto.getTeamAScore());
		assertEquals(3, responseDto.getTeamBScore());
		assertEquals(MatchStatus.CLOSED, responseDto.getMatchStatus());
		assertEquals(LocalDate.now(), responseDto.getMatchDate());
	}

	@Test
	@DisplayName("ID가 존재하지 않을 때 예외를 발생시키는지 확인합니다.")
	public void createMatchResult_MatchNotFound_ShouldThrowException() {
		// Given
		MatchResultRequestDto requestDto = new MatchResultRequestDto();
		requestDto.setMatchId(1L);

		when(matchRepository.findById(requestDto.getMatchId())).thenReturn(Optional.empty());

		// When & Then
		assertThrows(EntityNotFoundException.class, () -> {
			matchService.createMatchResult(requestDto);
		});
	}

	@Test
	@DisplayName("getMatchResult메소드의 정상 동작을 확인합니다.")
	public void getMatchResult_ShouldReturnMatchResultResponseDto() {
		// Given
		Long matchId = 1L;
		MatchResult matchResult = new MatchResult();
		matchResult.setId(1L);
		matchResult.setTeamAScore(2);
		matchResult.setTeamBScore(3);
		matchResult.setMatchStatus(MatchStatus.CLOSED);
		matchResult.setMatchDate(LocalDate.now());

		when(matchResultRepository.findByMatchId(matchId)).thenReturn(Optional.of(matchResult));

		// When
		MatchResultResponseDto responseDto = matchService.getMatchResult(matchId);

		// Then
		assertNotNull(responseDto);
		assertEquals(1L, responseDto.getId());
		assertEquals(2, responseDto.getTeamAScore());
		assertEquals(3, responseDto.getTeamBScore());
		assertEquals(MatchStatus.CLOSED, responseDto.getMatchStatus());
		assertEquals(LocalDate.now(), responseDto.getMatchDate());
	}

	@Test
	@DisplayName("ID가 존재하지 않을 때 예외를 발생시키는지 확인합니다.")
	public void getMatchResult_MatchResultNotFound_ShouldThrowException() {
		// Given
		Long matchId = 1L;

		when(matchResultRepository.findByMatchId(matchId)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(EntityNotFoundException.class, () -> {
			matchService.getMatchResult(matchId);
		});
	}

	@Test
	@DisplayName("날짜별 매치 조회 테스트")
	void getMatchesByDateTest() {
		LocalDate date = LocalDate.of(2024, 12, 31);
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		String cronDay = dayOfWeek.toString().substring(0, 3).toUpperCase();

		when(stadiumTimeRepository.findByCronDay(cronDay)).thenReturn(Arrays.asList(stadiumTime));
		when(matchRepository.findByStadiumTimeIdAndDateAndTime(eq(stadiumTime.getId()), eq(date), anyInt()))
			.thenReturn(Optional.of(match));

		MatchesByDateResponseDto responseDto = matchService.getMatchesByDate(date);

		//"0 0 08-10,10-12,20-22 ? * MON,TUE" 시간 3개 x 요일 2개
		assertEquals(6, responseDto.getData().size());//

		MatchByStadiumResponseDto matchResponse = responseDto.getData().get(0);
		assertEquals("A구장", matchResponse.getStadiumName());
		assertEquals("08:00", matchResponse.getStartTime());
		assertEquals("10:00", matchResponse.getEndTime());
		assertEquals(MatchStatus.ALMOST_FULL, matchResponse.getStatus(), "Match status should be PENDING");
	}
}