package com.sparta.sportify.service;

import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.dto.reservation.response.ReservationResponseDto;
import com.sparta.sportify.entity.*;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.ReservationRepository;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.util.cron.CronUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
    private StadiumTimeRepository stadiumTimeRepository;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationRequestDto requestDto;
    private StadiumTime stadiumTime;
    private Stadium stadium;
    private UserDetailsImpl authUser;

    @BeforeEach
    void setUp() {
        requestDto = new ReservationRequestDto();
        requestDto.setStadiumTimeId(1L);
        requestDto.setReservationDate(LocalDate.of(2024,12,3));
        requestDto.setTime(10);
        requestDto.setTeamColor(TeamColor.A);

        User user = new User();
        user.setId(1L);

        stadium = new Stadium(
                1L,
                "National Stadium",
                "Seoul, Korea",
                6,
                6,
                "12:00~20:00",
                "A large stadium for concerts.",
                500000,
                "Available",
                null,
                user
        );

        stadiumTime = new StadiumTime(1L, "0 0 9-11,12-14 ? * MON,TUE,WED,THU,FRI,SAT,SUN", stadium);

        authUser = mock(UserDetailsImpl.class);
        when(authUser.getUser()).thenReturn(user);
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
    }

    @Test
    @DisplayName("개인 예약에 대한 성공 케이스")
    void reservationPersonal_validReservation() {
        when(stadiumTimeRepository.findById(1L)).thenReturn(java.util.Optional.of(stadiumTime));
        when(reservationRepository.existsByUserAndMatchTimeAndReservationDate(any(), any(), any()))
                .thenReturn(false);
        when(matchRepository.findByDateAndTime(requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());

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
        when(matchRepository.findByDateAndTime(requestDto.getReservationDate(), requestDto.getTime()))
                .thenReturn(java.util.Optional.empty());

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

        when(matchRepository.findByDateAndTime(any(), any()))
                .thenReturn(Optional.of(existingMatch));  // 생성된 match 반환

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
        verify(reservationRepository, times(1)).save(any(Reservation.class)); // 예약 저장 호출 검증
    }


}
