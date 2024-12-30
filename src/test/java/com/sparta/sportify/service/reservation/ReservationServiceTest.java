package com.sparta.sportify.service.reservation;

import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.dto.reservation.response.ReservationFindResponseDto;
import com.sparta.sportify.dto.reservation.response.ReservationResponseDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.cashLog.CashLog;
import com.sparta.sportify.entity.cashLog.CashLogReservationMapping;
import com.sparta.sportify.entity.cashLog.CashType;
import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.reservation.Reservation;
import com.sparta.sportify.entity.reservation.ReservationStatus;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.team.TeamColor;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.*;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CashLogRepository cashLogRepository;

    @Mock
    private CashLogReservationMappingRepository cashLogReservationMappingRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StadiumTimeRepository stadiumTimeRepository;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationRequestDto requestDto;
    private StadiumTime stadiumTime;
    private Stadium stadium;
    private UserDetailsImpl authUser;
    private Team team;


    @BeforeEach
    void setUp() {
        requestDto = new ReservationRequestDto();
        requestDto.setStadiumTimeId(1L);
        requestDto.setReservationDate(LocalDate.of(2024, 12, 3));
        requestDto.setTime(10);
        requestDto.setTeamColor(TeamColor.A);

        User user = User.builder().id(1L).cash(200000L).build();

        team = Team.builder().id(1L).build();

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
                user
        );

        stadiumTime = new StadiumTime(1L, "0 0 9-11,12-14 ? * MON,TUE,WED,THU,FRI,SAT,SUN", stadium);

        authUser = mock(UserDetailsImpl.class);

        when(authUser.getUser()).thenReturn(user);
    }

    @Test
    void testReservationPersonal_TEAMA() {
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());

        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();

        CashLog cashLog = CashLog.builder()
                .id(1L)
                .price(stadium.getPrice())
                .type(CashType.PAYMENT)
                .user(authUser.getUser())
                .build();

        when(cashLogRepository.save(any())).thenReturn(cashLog);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        ReservationResponseDto response = reservationService.reservationPersonal(requestDto, authUser);


        assertNotNull(response);
        assertEquals(List.of(1L), response.getReservationId());
        assertEquals(150000L, authUser.getUser().getCash());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(cashLogRepository, times(1)).save(any(CashLog.class));
        verify(cashLogReservationMappingRepository, times(1)).save(any(CashLogReservationMapping.class));
    }

    @Test
    void testReservationPersonal_TEAMB() {
        requestDto.setTeamColor(TeamColor.B);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());

        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();

        CashLog cashLog = CashLog.builder()
                .id(1L)
                .price(stadium.getPrice())
                .type(CashType.PAYMENT)
                .user(authUser.getUser())
                .build();

        when(cashLogRepository.save(any())).thenReturn(cashLog);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        ReservationResponseDto response = reservationService.reservationPersonal(requestDto, authUser);


        assertNotNull(response);
        assertEquals(List.of(1L), response.getReservationId());
        assertEquals(150000L, authUser.getUser().getCash());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(cashLogRepository, times(1)).save(any(CashLog.class));
        verify(cashLogReservationMappingRepository, times(1)).save(any(CashLogReservationMapping.class));
    }

    @Test
    void testReservationPersonal_TEAMA_EXISTMATCH() {
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(Optional.of(match));


        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();

        CashLog cashLog = CashLog.builder()
                .id(1L)
                .price(stadium.getPrice())
                .type(CashType.PAYMENT)
                .user(authUser.getUser())
                .build();

        when(cashLogRepository.save(any())).thenReturn(cashLog);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        ReservationResponseDto response = reservationService.reservationPersonal(requestDto, authUser);


        assertNotNull(response);
        assertEquals(List.of(1L), response.getReservationId());
        assertEquals(150000L, authUser.getUser().getCash());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(cashLogRepository, times(1)).save(any(CashLog.class));
        verify(cashLogReservationMappingRepository, times(1)).save(any(CashLogReservationMapping.class));
    }

    @Test
    void testReservationPersonal_TEAMB_EXISTMATCH() {
        requestDto.setTeamColor(TeamColor.B);
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(Optional.of(match));


        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();

        CashLog cashLog = CashLog.builder()
                .id(1L)
                .price(stadium.getPrice())
                .type(CashType.PAYMENT)
                .user(authUser.getUser())
                .build();

        when(cashLogRepository.save(any())).thenReturn(cashLog);

        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        ReservationResponseDto response = reservationService.reservationPersonal(requestDto, authUser);


        assertNotNull(response);
        assertEquals(List.of(1L), response.getReservationId());
        assertEquals(150000L, authUser.getUser().getCash());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
        verify(cashLogRepository, times(1)).save(any(CashLog.class));
        verify(cashLogReservationMappingRepository, times(1)).save(any(CashLogReservationMapping.class));
    }


    @Test
    void testReservationPersonal_STADIUM_NOT_OPERATIONAL() {
        authUser.getUser();
        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.reservationPersonal(requestDto, authUser)
        );
        assertEquals(ErrorCode.STADIUM_NOT_OPERATIONAL, exception.getErrorCode());

        verify(reservationRepository, never()).save(any(Reservation.class)); // save 메서드가 호출되지 않아야 한다
    }

    @Test
    void testReservationPersonal_DUPLICATE_RESERVATION() {
        authUser.getUser();

        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));

        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(true);

        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.reservationPersonal(requestDto, authUser)
        );

        assertEquals(ErrorCode.DUPLICATE_RESERVATION, exception.getErrorCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testReservationPersonal_INVALID_OPERATION_TIME() {
        StadiumTime teststadiumTime = new StadiumTime(1L, "0 0 9-11,12-14 ? * MON", stadium);
        authUser.getUser();
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(teststadiumTime));

        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.reservationPersonal(requestDto, authUser)
        );

        assertEquals(ErrorCode.INVALID_OPERATION_TIME, exception.getErrorCode());

        verify(reservationRepository, never()).save(any(Reservation.class)); // save 메서드가 호출되지 않아야 한다
    }

    @Test
    void testReservationPersonal_TEAMA_NOT_ENOUGH_SPOTS_FOR_TEAM() {
        stadium = new Stadium(
                1L,
                "National Stadium",
                "Seoul, Korea",
                0,
                0,
                "A large stadium for concerts.",
                50000L,
                StadiumStatus.APPROVED,
                null,
                authUser.getUser()
        );

        stadiumTime = new StadiumTime(1L, "0 0 9-11,12-14 ? * MON,TUE,WED,THU,FRI,SAT,SUN", stadium);

        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());

//        ReservationResponseDto response = reservationService.reservationPersonal(requestDto, authUser);
        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.reservationPersonal(requestDto, authUser)
        );
        assertEquals(ErrorCode.NOT_ENOUGH_SPOTS_FOR_TEAM, exception.getErrorCode());

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void testReservationPersonal_TEAMB_NOT_ENOUGH_SPOTS_FOR_TEAM() {
        requestDto.setTeamColor(TeamColor.B);
        stadium = new Stadium(
                1L,
                "National Stadium",
                "Seoul, Korea",
                0,
                0,
                "A large stadium for concerts.",
                50000L,
                StadiumStatus.APPROVED,
                null,
                authUser.getUser()
        );

        stadiumTime = new StadiumTime(1L, "0 0 9-11,12-14 ? * MON,TUE,WED,THU,FRI,SAT,SUN", stadium);

        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());

//        ReservationResponseDto response = reservationService.reservationPersonal(requestDto, authUser);
        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.reservationPersonal(requestDto, authUser)
        );
        assertEquals(ErrorCode.NOT_ENOUGH_SPOTS_FOR_TEAM, exception.getErrorCode());

        verify(reservationRepository, never()).save(any(Reservation.class));
    }


    @Test
    void testReservationGroup_TEAMA() {
        requestDto.setTeamId(1);
        requestDto.setTeamMemberIdList(List.of(2L, 3L, 4L));

        User user1 = User.builder().id(2L).build();
        User user2 = User.builder().id(3L).build();
        User user3 = User.builder().id(4L).build();

        List<User> users = List.of(user1, user2, user3);

        when(teamRepository.findById(any())).thenReturn(Optional.of(team));
        when(userRepository.findUsersByIdIn(requestDto.getTeamMemberIdList())).thenReturn(users);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUsersAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());

        Reservation mockReservation1 = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .team(team)
                .user(user1)
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();
        Reservation mockReservation2 = Reservation.builder()
                .id(2L)
                .reservationDate(requestDto.getReservationDate())
                .team(team)
                .user(user1)
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();
        Reservation mockReservation3 = Reservation.builder()
                .id(3L)
                .reservationDate(requestDto.getReservationDate())
                .team(team)
                .user(user1)
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(mockReservation1)
                .thenReturn(mockReservation2)
                .thenReturn(mockReservation3);

        CashLog cashLog = CashLog.builder()
                .id(1L)
                .price(stadium.getPrice())
                .type(CashType.PAYMENT)
                .user(authUser.getUser())
                .build();

        when(cashLogRepository.save(any())).thenReturn(cashLog);


        ReservationResponseDto response = reservationService.reservationGroup(requestDto, authUser);


        assertNotNull(response);
        assertEquals(users.size(), response.getReservationId().size());
        assertEquals(50000L, authUser.getUser().getCash());

        verify(reservationRepository, times(3)).save(any(Reservation.class));
        verify(cashLogRepository, times(1)).save(any(CashLog.class));
        verify(cashLogReservationMappingRepository, times(3)).save(any(CashLogReservationMapping.class));
    }

    @Test
    void testReservationGroup_TEAMB() {
        requestDto.setTeamColor(TeamColor.B);
        requestDto.setTeamId(1);
        requestDto.setTeamMemberIdList(List.of(2L, 3L, 4L));

        User user1 = User.builder().id(2L).build();
        User user2 = User.builder().id(3L).build();
        User user3 = User.builder().id(4L).build();

        List<User> users = List.of(user1, user2, user3);

        when(teamRepository.findById(any())).thenReturn(Optional.of(team));
        when(userRepository.findUsersByIdIn(requestDto.getTeamMemberIdList())).thenReturn(users);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUsersAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(), requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());

        Reservation mockReservation1 = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .team(team)
                .user(user1)
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();
        Reservation mockReservation2 = Reservation.builder()
                .id(2L)
                .reservationDate(requestDto.getReservationDate())
                .team(team)
                .user(user1)
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();
        Reservation mockReservation3 = Reservation.builder()
                .id(3L)
                .reservationDate(requestDto.getReservationDate())
                .team(team)
                .user(user1)
                .teamColor(requestDto.getTeamColor())
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(mockReservation1)
                .thenReturn(mockReservation2)
                .thenReturn(mockReservation3);

        CashLog cashLog = CashLog.builder()
                .id(1L)
                .price(stadium.getPrice())
                .type(CashType.PAYMENT)
                .user(authUser.getUser())
                .build();

        when(cashLogRepository.save(any())).thenReturn(cashLog);


        ReservationResponseDto response = reservationService.reservationGroup(requestDto, authUser);


        assertNotNull(response);
        assertEquals(users.size(), response.getReservationId().size());
        assertEquals(50000L, authUser.getUser().getCash());

        verify(reservationRepository, times(3)).save(any(Reservation.class));
        verify(cashLogRepository, times(1)).save(any(CashLog.class));
        verify(cashLogReservationMappingRepository, times(3)).save(any(CashLogReservationMapping.class));
    }

    @Test
    void testReservationGroup_STADIUM_NOT_OPERATIONAL() {
        requestDto.setTeamId(1);
        requestDto.setTeamMemberIdList(List.of(2L, 3L, 4L));
        authUser.getUser();

        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.reservationGroup(requestDto, authUser)
        );
        assertEquals(ErrorCode.STADIUM_NOT_OPERATIONAL, exception.getErrorCode());

        verify(reservationRepository, never()).save(any(Reservation.class)); // save 메서드가 호출되지 않아야 한다
    }

    @Test
    void testReservationGroup_USER_INFO_INVALID() {
        requestDto.setTeamId(1);
        requestDto.setTeamMemberIdList(List.of(2L, 3L, 4L));
        authUser.getUser();

        User user1 = User.builder().id(2L).build();
        User user2 = User.builder().id(3L).build();

        List<User> users = List.of(user1, user2);

        when(userRepository.findUsersByIdIn(requestDto.getTeamMemberIdList())).thenReturn(users);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));

        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.reservationGroup(requestDto, authUser)
        );
        assertEquals(ErrorCode.USER_INFO_INVALID, exception.getErrorCode());

        verify(reservationRepository, never()).save(any(Reservation.class)); // save 메서드가 호출되지 않아야 한다
    }

    @Test
    void testReservationGroup_DUPLICATE_RESERVATION() {
        requestDto.setTeamId(1);
        requestDto.setTeamMemberIdList(List.of(2L, 3L, 4L));
        authUser.getUser();

        User user1 = User.builder().id(2L).build();
        User user2 = User.builder().id(3L).build();
        User user3 = User.builder().id(4L).build();
        List<User> users = List.of(user1, user2, user3);

        when(userRepository.findUsersByIdIn(requestDto.getTeamMemberIdList())).thenReturn(users);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUsersAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(true);

        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.reservationGroup(requestDto, authUser)
        );
        assertEquals(ErrorCode.DUPLICATE_RESERVATION, exception.getErrorCode());

        verify(reservationRepository, never()).save(any(Reservation.class)); // save 메서드가 호출되지 않아야 한다
    }

    @Test
    void testReservationGroup_TEAM_NOT_FOUND() {
        requestDto.setTeamId(1);
        requestDto.setTeamMemberIdList(List.of(2L, 3L, 4L));
        authUser.getUser();

        User user1 = User.builder().id(2L).build();
        User user2 = User.builder().id(3L).build();
        User user3 = User.builder().id(4L).build();

        List<User> users = List.of(user1, user2, user3);

        when(userRepository.findUsersByIdIn(requestDto.getTeamMemberIdList())).thenReturn(users);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(teamRepository.findById(any())).thenReturn(Optional.empty());

        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.reservationGroup(requestDto, authUser)
        );
        assertEquals(ErrorCode.TEAM_NOT_FOUND, exception.getErrorCode());

        verify(reservationRepository, never()).save(any(Reservation.class)); // save 메서드가 호출되지 않아야 한다
    }

    @Test
    public void testFindReservation() {
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 12, 3))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.A)
                .match(match)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));


        ReservationFindResponseDto responseDto = reservationService.findReservation(reservation.getId(), authUser);

        assertNotNull(responseDto);
        assertEquals(reservation.getId(), responseDto.getReservationId());
        assertEquals(reservation.getUser().getId(), authUser.getUser().getId());
    }

    @Test
    public void testFindReservation_RESERVATION_NOT_FOUND() {
        User user2 = User.builder().id(2L).build();
        authUser.getUser();
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 12, 3))
                .team(team)
                .user(user2)
                .teamColor(TeamColor.A)
                .match(match)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.findReservation(reservation.getId(), authUser)
        );
        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());


    }

    @Test
    public void testFindReservation_USER_INFO_MISMATCH() {
        User user2 = User.builder().id(2L).build();

        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 12, 3))
                .team(team)
                .user(user2)
                .teamColor(TeamColor.A)
                .match(match)
                .status(ReservationStatus.CONFIRMED)
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // When & Then
        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.findReservation(reservation.getId(), authUser)
        );
        assertEquals(ErrorCode.USER_INFO_MISMATCH, exception.getErrorCode());


    }

    @Test
    public void testFindReservationsForInfiniteScroll() {
        // Given

        Match match1 = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 12, 3))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.A)
                .match(match1)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Match match2 = Match.builder()
                .id(2L)
                .date(LocalDate.of(2024, 12, 4))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2L)
                .reservationDate(LocalDate.of(2024, 12, 4))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.A)
                .match(match2)
                .status(ReservationStatus.CONFIRMED)
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        List<Reservation> mockReservations = List.of(reservation1, reservation2);
        Slice<Reservation> mockSlice = new SliceImpl<>(mockReservations, pageable, true);

        when(reservationRepository.findByUserIdOrderByIdDesc(authUser.getUser().getId(), pageable))
                .thenReturn(mockSlice);
        // When
        Slice<ReservationFindResponseDto> responseDtos = reservationService.findReservationsForInfiniteScroll(authUser, pageable);

        // Then
        assertNotNull(responseDtos);
        assertEquals(2, responseDtos.getContent().size());
        assertTrue(responseDtos.hasNext());
    }

    @Test
    @DisplayName("예약 취소 성공")
    void deleteReservation() {
        Long reservationId = 1L;
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(5)
                .bTeamCount(6)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 12, 3))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.A)
                .match(match)
                .status(ReservationStatus.CONFIRMED)
                .build();

        ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        ReservationResponseDto responseDto = reservationService.deleteReservation(reservation.getId(), authUser);

        assertEquals(List.of(reservationId), responseDto.getReservationId());

        verify(matchRepository, times(1)).save(matchCaptor.capture());
        verify(reservationRepository, times(1)).save(reservationCaptor.capture());

        Match savedMatch = matchCaptor.getValue();
        Reservation savedReservation = reservationCaptor.getValue();

        assertEquals(ReservationStatus.CANCELED, savedReservation.getStatus());
        assertEquals(6, savedMatch.getATeamCount());
        assertEquals(6, savedMatch.getBTeamCount());
    }

    @Test
    @DisplayName("예약 취소 성공")
    void deleteReservation_TEAMB() {
        Long reservationId = 1L;
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(5)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 12, 3))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.B)
                .match(match)
                .status(ReservationStatus.CONFIRMED)
                .build();

        ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        ReservationResponseDto responseDto = reservationService.deleteReservation(reservation.getId(), authUser);

        assertEquals(List.of(reservationId), responseDto.getReservationId());

        verify(matchRepository, times(1)).save(matchCaptor.capture());
        verify(reservationRepository, times(1)).save(reservationCaptor.capture());

        Match savedMatch = matchCaptor.getValue();
        Reservation savedReservation = reservationCaptor.getValue();

        assertEquals(ReservationStatus.CANCELED, savedReservation.getStatus());
        assertEquals(6, savedMatch.getATeamCount());
        assertEquals(6, savedMatch.getBTeamCount());
    }

    @Test
    void deleteReservation_RESERVATION_NOT_FOUND() {
        Long reservationId = 1L;
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(5)
                .bTeamCount(6)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 12, 3))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.A)
                .match(match)
                .status(ReservationStatus.CONFIRMED)
                .build();

        ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());


        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.deleteReservation(reservation.getId(), authUser)
        );
        assertEquals(ErrorCode.RESERVATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void deleteReservation_USER_INFO_MISMATCH() {
        Long reservationId = 1L;
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(5)
                .bTeamCount(6)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 12, 3))
                .team(team)
                .user(User.builder().id(4L).build())
                .teamColor(TeamColor.A)
                .match(match)
                .status(ReservationStatus.CONFIRMED)
                .build();

        ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));


        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.deleteReservation(reservation.getId(), authUser)
        );
        assertEquals(ErrorCode.USER_INFO_MISMATCH, exception.getErrorCode());
    }

    @Test
    void deleteReservation_MATCH_NOT_FOUND() {
        Long reservationId = 1L;
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024, 12, 3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(5)
                .bTeamCount(6)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024, 12, 3))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.A)
                .match(match)
                .status(ReservationStatus.CONFIRMED)
                .build();

        ArgumentCaptor<Match> matchCaptor = ArgumentCaptor.forClass(Match.class);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(matchRepository.findById(1L)).thenReturn(Optional.empty());

        CustomApiException exception = assertThrows(CustomApiException.class, () ->
                reservationService.deleteReservation(reservation.getId(), authUser)
        );
        assertEquals(ErrorCode.MATCH_NOT_FOUND, exception.getErrorCode());
    }

}
