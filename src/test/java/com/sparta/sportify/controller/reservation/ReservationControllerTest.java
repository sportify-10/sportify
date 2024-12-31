package com.sparta.sportify.controller.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.dto.reservation.request.ReservationRequestDto;
import com.sparta.sportify.dto.reservation.response.ReservationFindResponseDto;
import com.sparta.sportify.dto.reservation.response.ReservationResponseDto;
import com.sparta.sportify.entity.StadiumTime.StadiumTime;
import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.reservation.Reservation;
import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.team.TeamColor;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    @Mock
    private UserDetailsImpl authUser;

    private ReservationRequestDto reservationRequestDto;

    @BeforeEach
    void setUp() {
        reservationRequestDto = new ReservationRequestDto();
        reservationRequestDto.setTeamColor(TeamColor.A);
        reservationRequestDto.setStadiumTimeId(1L);
        reservationRequestDto.setTime(10);
        reservationRequestDto.setTeamMemberIdList(new ArrayList<>());
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testReservationProcessPersonal() throws Exception {
        // Given
        ReservationResponseDto reservationResponseDto = new ReservationResponseDto();
        when(reservationService.reservationPersonal(any(), any())).thenReturn(reservationResponseDto);

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(reservationRequestDto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("개인예약 성공"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testReservationProcessGroup() throws Exception {
        // Given
        reservationRequestDto.setTeamMemberIdList(List.of(1L, 2L));  // 단체 예약
        ReservationResponseDto reservationResponseDto = new ReservationResponseDto();
        when(reservationService.reservationGroup(any(), any())).thenReturn(reservationResponseDto);

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(reservationRequestDto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("단체예약 성공"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testFindAllReservations() throws Exception {
        // Given
        Slice<ReservationFindResponseDto> reservations = new SliceImpl<>(List.of(new ReservationFindResponseDto(
                Reservation.builder()
                        .id(1L)
                        .user(User.builder().id(1L).build())
                        .totalAmount(1000L)
                        .reservationDate(LocalDate.of(2024, 12, 27))
                        .match(Match.builder().stadiumTime(StadiumTime.builder().stadium(Stadium.builder().id(1L).build()).build()).build())
                        .build()
        )));

        when(reservationService.findReservationsForInfiniteScroll(any(), any())).thenReturn(reservations);

        // When & Then
        mockMvc.perform(get("/api/reservations")
                        .param("page", "0") // 첫 페이지
                        .param("size", "10") // 한 페이지에 10개
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 조회 성공"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testFindReservationById() throws Exception {
        // Given
        Long reservationId = 1L;
        ReservationFindResponseDto reservation = mock(ReservationFindResponseDto.class);
        when(reservationService.findReservation(reservationId, authUser)).thenReturn(reservation);

        // When & Then
        mockMvc.perform(get("/api/reservations/{reservationId}", reservationId)
                        .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 조회 성공"));
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testDeleteReservation() throws Exception {
        // Given
        Long reservationId = 1L;
        ReservationResponseDto reservationResponseDto = new ReservationResponseDto();
        when(reservationService.deleteReservation(reservationId, authUser)).thenReturn(reservationResponseDto);

        // When & Then
        mockMvc.perform(delete("/api/reservations/{reservationId}", reservationId)
                        .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("예약 취소 성공"));
    }
}