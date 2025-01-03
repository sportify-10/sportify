package com.sparta.sportify.service.match;

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
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.*;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    private User user;
    private UserDetailsImpl userDetails;
    private Stadium stadium2;
    private StadiumTime stadiumTime2;
    private Match match2;

    @BeforeEach
    void setUp() {
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

        stadium2 = Stadium.builder()
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

        stadiumTime2 = StadiumTime.builder()
                .id(1L)
                .cron("0 0 08-10,10-12,20-22 ? * MON,TUE")
                .stadium(stadium2)
                .build();

        match2 = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 31))
                .time(20)
                .aTeamCount(4)
                .bTeamCount(6)
                .stadiumTime(stadiumTime2)
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
    @DisplayName("A팀 승리, 점수 추가 로직 테스트")
    void createMatchResult_TeamAWin_SavesMatchResultAndUpdatesPoints() {
        requestDto = new MatchResultRequestDto(10, 5, MatchStatus.CLOSED, 1L); // 요청 DTO 초기화

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
        assertEquals(1010, userA.getLevelPoints()); // userA는 10점 증가
        assertEquals(990, userB.getLevelPoints()); // userB는 -10점 감소

        // Team points verification
        //verify(teamRepository, times(1)).save(any(Team.class)); // 팀 점수 업데이트
        assertEquals(1010, teamA.getTeamPoints()); // Team A는 10점 증가
        assertEquals(990, teamB.getTeamPoints()); // Team B는 -10점 감소
    }

    @Test
    @DisplayName("B팀 승리, 점수 추가 로직 테스트")
    void createMatchResult_TeamBWin_SavesMatchResultAndUpdatesPoints() {
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
    @DisplayName("무승부, 점수 추가 로직 테스트")
    void createMatchResult_Draw_SavesMatchResultAndUpdatesPoints() {
        requestDto = new MatchResultRequestDto(10, 10, MatchStatus.CLOSED, 1L); // 요청 DTO 초기화

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
        assertEquals(1005, userA.getLevelPoints()); // userA는 10점 증가
        assertEquals(1005, userB.getLevelPoints()); // userB는 -10점 감소

        // Team points verification
        //verify(teamRepository, times(1)).save(any(Team.class)); // 팀 점수 업데이트
        assertEquals(1005, teamA.getTeamPoints()); // Team A는 10점 증가
        assertEquals(1005, teamB.getTeamPoints()); // Team B는 -10점 감소
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
    @DisplayName("매치 단건조회 성공, CLOSED")
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
    @DisplayName("매치 단건조회 성공, ALMOST_FULL")
    void getMatchByDateAndTime_ReturnsMatchDetailResponseDto_WhenMatchFound_ALMOST_FULL() {
        matchResult = MatchResult.builder()
                .id(1L)
                .teamAScore(10)
                .teamBScore(5)
                .matchStatus(MatchStatus.ALMOST_FULL)
                .matchDate(LocalDate.now())
                .build();
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
    @DisplayName("매치 단건조회 성공, OPEN")
    void getMatchByDateAndTime_ReturnsMatchDetailResponseDto_WhenMatchFound_OPEN() {
        matchResult = MatchResult.builder()
                .id(1L)
                .teamAScore(10)
                .teamBScore(5)
                .matchStatus(MatchStatus.OPEN)
                .matchDate(LocalDate.now())
                .build();
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

    @Test
    @DisplayName("날짜별 매치 조회 성공")
    void getMatchesByDate_ReturnsMatches_WhenMatchesExist() {
        // Given
        LocalDate date = LocalDate.of(2024, 12, 27);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String cronDay = dayOfWeek.toString().substring(0, 3).toUpperCase(); // "FRI"

        when(stadiumTimeRepository.findByCronDay(cronDay)).thenReturn(Arrays.asList(stadiumTime));
        // when(matchRepository.findByStadiumTimeIdAndDateAndTime(any(), any(), any())).thenReturn(
        // 	Optional.of(match));

        // When
        MatchesByDateResponseDto response = matchService.getMatchesByDate(date);

        // Then
        assertNotNull(response);
    }

    @Test
    @DisplayName("날짜별 매치가 존재하지 않을때, 서비스가 올바르게 동작하는지 확인")
    void getMatchesByDate_ReturnsEmptyList_WhenNoMatchesFound() {
        // Given
        LocalDate date = LocalDate.of(2024, 12, 27);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String cronDay = dayOfWeek.toString().substring(0, 3).toUpperCase(); // "FRI"

        when(stadiumTimeRepository.findByCronDay(cronDay)).thenReturn(Arrays.asList(stadiumTime));
        // when(matchRepository.findByStadiumTimeIdAndDateAndTime(stadiumTime.getId(), date, 14)).thenReturn(
        // 	Optional.empty());

        // When
        MatchesByDateResponseDto response = matchService.getMatchesByDate(date);

        // Then
        assertNotNull(response);
    }

    @Test
    @DisplayName("스타디움 타임이 존재하지 않을때 서비스가 올바르게 동작하는지 확인")
    void getMatchesByDate_ReturnsEmptyList_WhenStadiumTimeNotFound() {
        // Given
        LocalDate date = LocalDate.of(2024, 12, 27);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String cronDay = dayOfWeek.toString().substring(0, 3).toUpperCase(); // "FRI"

        when(stadiumTimeRepository.findByCronDay(cronDay)).thenReturn(Arrays.asList(stadiumTime));
        // when(matchRepository.findByStadiumTimeIdAndDateAndTime(stadiumTime.getId(), date, 14)).thenReturn(
        // 	Optional.empty());

        // When
        MatchesByDateResponseDto response = matchService.getMatchesByDate(date);

        // Then
        assertNotNull(response);
    }

    @Test
    @DisplayName("매치 상태 로직 테스트 성공, CLOSED")
    public void testMatchStatusClosed() {
        // Stadium 객체 생성
        Stadium stadium = Stadium.builder()
                .aTeamCount(10)
                .bTeamCount(10)
                .build();
        // StadiumTime 객체 생성
        StadiumTime stadiumTime = StadiumTime.builder()
                .stadium(stadium)
                .build();
        // 경기가 종료된 상태
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.now())
                .time(10)
                .aTeamCount(0)
                .bTeamCount(0)
                .stadiumTime(stadiumTime)
                .build();

        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(15, 0));
        MatchStatus status = matchService.determineMatchStatus(match, now);

        assertEquals(MatchStatus.CLOSED, status);
    }

    @Test
    @DisplayName("매치 상태 로직 테스트 성공, ALMOST_FULL, 인원수 80%")
    public void testMatchStatusAlmostFull_BeforeStart() {
        // Stadium 객체 생성
        Stadium stadium = Stadium.builder()
                .aTeamCount(10)
                .bTeamCount(10)
                .build();
        // StadiumTime 객체 생성
        StadiumTime stadiumTime = StadiumTime.builder()
                .stadium(stadium)
                .build();
        // 경기 시작 전 예약 비율이 80% 이상
        Match match = Match.builder()
                .id(2L)
                .date(LocalDate.now())
                .time(10)
                .aTeamCount(8)
                .bTeamCount(8)
                .stadiumTime(stadiumTime)
                .build();

        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 0)); // 현재 시간 09:00
        MatchStatus status = matchService.determineMatchStatus(match, now);

        assertEquals(MatchStatus.ALMOST_FULL, status);
    }

    @Test
    @DisplayName("매치 상태 로직 테스트 성공, ALMOST_FULL, 시작 시간 4시간 이내")
    public void testMatchStatusAlmostFull_Within4Hours() {
        // Stadium 객체 생성
        Stadium stadium = Stadium.builder()
                .aTeamCount(10)
                .bTeamCount(10)
                .build();
        // StadiumTime 객체 생성
        StadiumTime stadiumTime = StadiumTime.builder()
                .stadium(stadium)
                .build();
        // 경기 시작 4시간 이내
        Match match = Match.builder()
                .id(3L)
                .date(LocalDate.now())
                .time(10)
                .aTeamCount(2)
                .bTeamCount(1)
                .stadiumTime(stadiumTime)
                .build();

        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)); // 현재 시간 06:00
        MatchStatus status = matchService.determineMatchStatus(match, now);

        assertEquals(MatchStatus.ALMOST_FULL, status);
    }

    @Test
    @DisplayName("매치 상태 로직 테스트 성공, OPEN")
    public void testMatchStatusOpen() {
        // Stadium 객체 생성
        Stadium stadium = Stadium.builder()
                .aTeamCount(10)
                .bTeamCount(10)
                .build();
        // StadiumTime 객체 생성
        StadiumTime stadiumTime = StadiumTime.builder()
                .stadium(stadium)
                .build();
        // 경기 시작 시간이 멀고 예약 비율이 낮음
        Match match = Match.builder()
                .id(4L)
                .date(LocalDate.now())
                .time(20)
                .aTeamCount(1)
                .bTeamCount(1)
                .stadiumTime(stadiumTime)
                .build();

        LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(10, 0)); // 현재 시간 09:00
        MatchStatus status = matchService.determineMatchStatus(match, now);

        assertEquals(MatchStatus.OPEN, status);
    }

    @Test
    @DisplayName("날짜별 매치 조회 테스트")
    void getMatchesByDateTest() {
        LocalDate date = LocalDate.of(2024, 12, 31);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        String cronDay = dayOfWeek.toString().substring(0, 3).toUpperCase();

        when(stadiumTimeRepository.findByCronDay(cronDay)).thenReturn(Arrays.asList(stadiumTime2));
        when(matchRepository.findByStadiumTimeIdAndDateAndTime(eq(stadiumTime2.getId()), eq(date), anyInt()))
                .thenReturn(Optional.of(match2));

        MatchesByDateResponseDto responseDto = matchService.getMatchesByDate(date);

        //"0 0 08-10,10-12,20-22 ? * MON,TUE" 시간 3개 x 요일 2개
        assertEquals(3, responseDto.getData().size());//

        MatchByStadiumResponseDto matchResponse = responseDto.getData().get(0);
        assertEquals("A구장", matchResponse.getStadiumName());
        assertEquals("08:00", matchResponse.getStartTime());
        assertEquals("10:00", matchResponse.getEndTime());
        assertEquals(MatchStatus.CLOSED, matchResponse.getStatus(), "Match status should be PENDING");
    }
}