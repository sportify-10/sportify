package com.sparta.sportify.service.reservation;

import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.dto.reservation.response.ReservationFindResponseDto;
import com.sparta.sportify.dto.reservation.response.ReservationResponseDto;
import com.sparta.sportify.entity.*;
import com.sparta.sportify.repository.*;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.ReservationService;
import com.sparta.sportify.util.cron.CronUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

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
        requestDto.setReservationDate(LocalDate.of(2024,12,3));
        requestDto.setTime(10);
        requestDto.setTeamColor(TeamColor.A);

        User user = new User();
        user.setId(1L);

        team = Team.builder().id(1L).build();


        stadium = new Stadium(
                1L,
                "National Stadium",
                "Seoul, Korea",
                6,
                6,
                "A large stadium for concerts.",
                500000,
                StadiumStatus.APPROVED,
                null,
                user
        );

        stadiumTime = new StadiumTime(1L, "0 0 9-11,12-14 ? * MON,TUE,WED,THU,FRI,SAT,SUN", stadium);

        authUser = mock(UserDetailsImpl.class);
        when(authUser.getUser()).thenReturn(user);

    }

    @Test
    @DisplayName("개인 예약에 대한 성공 케이스")
    void reservationPersonal_validReservation() {
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(),requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));

        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .teamColor(requestDto.getTeamColor())
                .status("예약중")
                .build();

        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        ReservationResponseDto response = reservationService.reservationPersonal(requestDto, authUser);

        assertNotNull(response);
        assertEquals(1L, response.getReservationId()); // Ensure reservationId is correctly returned
        verify(reservationRepository, times(1)).save(any(Reservation.class)); // Verify save method is called once
    }

    @Test
    @DisplayName("개인 예약에 대한 예약할 수 없는 시간에 예약한 경우")
    void reservationPersonal_invalidCron() {
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(CronUtil.isCronDateAllowed(stadiumTime.getCron(), requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(false);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reservationService.reservationPersonal(requestDto, authUser)
        );

        assertEquals("구장 운영시간이 맞지 않습니다.", exception.getMessage());
        verify(reservationRepository, never()).save(any(Reservation.class)); // save 메서드가 호출되지 않아야 한다
    }

    @Test
    @DisplayName("이미 예약이 존재하는 경우")
    void reservationPersonal_alreadyReserved() {

        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(true);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                reservationService.reservationPersonal(requestDto, authUser)
        );

        assertEquals("이미 중복된 시간에 예약을 하였습니다.", exception.getMessage());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("개인예약 : match정보가 없을 경우 생성에 대한 성공 케이스")
    void reservationPersonal_createNewMatch() {
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(),requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));

        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .teamColor(requestDto.getTeamColor())
                .status("예약중")
                .build();

        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        ReservationResponseDto response = reservationService.reservationPersonal(requestDto, authUser);

        assertNotNull(response);
        assertEquals(1L, response.getReservationId());
        verify(matchRepository, times(1)).save(any(Match.class));
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("개인예약 : 이미 match가 DB에 들어가 있을 경우 성공 케이스")
    void reservationPersonal_createExistingMatch() {

        Match existingMatch = Match.builder()
                .id(1L)
                .date(requestDto.getReservationDate())
                .time(requestDto.getTime())
                .aTeamCount(6)
                .bTeamCount(6)
                .stadiumTime(stadiumTime)
                .build();

        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);

        when(matchRepository.findByIdAndDateAndTime(any(),any(), any()))
                .thenReturn(Optional.of(existingMatch));

        when(matchRepository.save(any()))
                .thenReturn(existingMatch);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));

        Reservation mockReservation = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .teamColor(requestDto.getTeamColor())
                .status("예약중")
                .build();


        when(reservationRepository.save(any(Reservation.class))).thenReturn(mockReservation);

        ReservationResponseDto response = reservationService.reservationPersonal(requestDto, authUser);

        assertNotNull(response);
//        assertEquals(1L, response.getReservationId());

        verify(matchRepository, times(1)).save(any(Match.class));
        verify(reservationRepository, times(1)).save(any(Reservation.class)); // 예약 저장 호출 검증
    }




    @Test
    @DisplayName("단체 예약에 대한 성공 케이스")
    void reservationGroup_validReservation() {
        // Additional setup
        requestDto.setTeamId(1);
        requestDto.setTeamMemberIdList(List.of(1L,2L,3L));

        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user1.setId(2L);
        User user3 = new User();
        user1.setId(3L);

        List<User> users = List.of(user1,user2,user3);

        when(teamRepository.findById(any())).thenReturn(Optional.of(team));
        when(userRepository.findUsersByIdIn(requestDto.getTeamMemberIdList())).thenReturn(users);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByIdAndDateAndTime(requestDto.getStadiumTimeId(),requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));

        Reservation mockReservation1 = Reservation.builder()
                .id(1L)
                .reservationDate(requestDto.getReservationDate())
                .team(team)
                .user(user1)
                .teamColor(requestDto.getTeamColor())
                .status("예약중")
                .build();
        Reservation mockReservation2 = Reservation.builder()
                .id(2L)
                .reservationDate(requestDto.getReservationDate())
                .team(team)
                .user(user1)
                .teamColor(requestDto.getTeamColor())
                .status("예약중")
                .build();
        Reservation mockReservation3 = Reservation.builder()
                .id(3L)
                .reservationDate(requestDto.getReservationDate())
                .team(team)
                .user(user1)
                .teamColor(requestDto.getTeamColor())
                .status("예약중")
                .build();

        when(reservationRepository.save(any(Reservation.class)))
                .thenReturn(mockReservation1)
                .thenReturn(mockReservation2)
                .thenReturn(mockReservation3);

        ReservationResponseDto response = reservationService.reservationGroup(requestDto, authUser);


        assertNotNull(response);
        assertEquals(users.size(), response.getReservationId().size());
        verify(reservationRepository, times(3)).save(any(Reservation.class));
    }
    @Test
    @DisplayName("단건 예약 조회 성공")
    public void testFindReservation_Success() {
        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024,12,3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024,12,3))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.A)
                .match(match)
                .status("예약중")
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));


        ReservationFindResponseDto responseDto = reservationService.findReservation(reservation.getId(), authUser);

        assertNotNull(responseDto);
        assertEquals(reservation.getId(), responseDto.getReservationId());
        assertEquals(reservation.getUser().getId(), authUser.getUser().getId());
    }

    @Test
    @DisplayName("단건 예약 조회 실패 - 다른 유저")
    public void testFindReservation_Fail_UnauthorizedUser() {
        User user2 = new User();
        user2.setId(2L);

        Match match = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024,12,3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        Reservation reservation = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024,12,3))
                .team(team)
                .user(user2)
                .teamColor(TeamColor.A)
                .match(match)
                .status("예약중")
                .build();

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reservationService.findReservation(reservation.getId(), authUser);
        });
    }

    @Test
    @DisplayName("무한 스크롤 예약 조회 성공")
    public void testFindReservationsForInfiniteScroll_Success() {
        // Given

        Match match1 = Match.builder()
                .id(1L)
                .date(LocalDate.of(2024,12,3))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        Reservation reservation1 = Reservation.builder()
                .id(1L)
                .reservationDate(LocalDate.of(2024,12,3))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.A)
                .match(match1)
                .status("예약중")
                .build();

        Match match2 = Match.builder()
                .id(2L)
                .date(LocalDate.of(2024,12,4))
                .time(10)
                .stadiumTime(stadiumTime)
                .aTeamCount(6)
                .bTeamCount(6)
                .build();

        Reservation reservation2 = Reservation.builder()
                .id(2L)
                .reservationDate(LocalDate.of(2024,12,4))
                .team(team)
                .user(authUser.getUser())
                .teamColor(TeamColor.A)
                .match(match2)
                .status("예약중")
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

}
