package com.sparta.sportify.controller.match;

import com.sparta.sportify.dto.match.MatchDetailResponseDto;
import com.sparta.sportify.dto.match.MatchResultRequestDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.dto.match.response.MatchByStadiumResponseDto;
import com.sparta.sportify.dto.match.response.MatchesByDateResponseDto;
import com.sparta.sportify.entity.matchResult.MatchStatus;
import com.sparta.sportify.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MatchControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchController matchController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(matchController).build();
    }

    @Test
    @DisplayName("매치 조회 성공 테스트")
    public void testGetMatchByDateAndTime() throws Exception {
        // Given
        Long stadiumId = 1L;
        LocalDate date = LocalDate.now();
        String time = "14";
        MatchDetailResponseDto responseDto = new MatchDetailResponseDto(
                1L,
                date,
                "14",
                1,
                2,
                "Team B",
                MatchStatus.OPEN
        );

        when(matchService.getMatchByDateAndTime(any(), any(), any(), any()))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/matches/{stadiumId}/{date}/{time}", stadiumId, date, time)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("매치 조회 성공"));
    }

    @Test
    @DisplayName("경기 결과 조회 테스트 성공")
    public void testGetMatchResult() throws Exception {
        // Given
        Long matchId = 1L;

        MatchResultResponseDto responseDto = new MatchResultResponseDto(
                matchId,
                1,
                2,
                MatchStatus.CLOSED,
                LocalDate.now()
        );

        when(matchService.getMatchResult(matchId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/matches/result/{matchId}", matchId) // GET 요청 URL
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("경기 결과 조회"));
    }

    @Test
    public void testCreateMatchResult() throws Exception {
        // Given
        Long matchId = 1L;

        MatchResultRequestDto requestDto = new MatchResultRequestDto();
        requestDto.setTeamAScore(1);
        requestDto.setTeamBScore(2);
        requestDto.setMatchStatus(MatchStatus.CLOSED);
        requestDto.setMatchId(matchId);

        MatchResultResponseDto responseDto = new MatchResultResponseDto(
                matchId,
                requestDto.getTeamAScore(),
                requestDto.getTeamBScore(),
                requestDto.getMatchStatus(),
                LocalDate.now()
        );

        when(matchService.createMatchResult(any(MatchResultRequestDto.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/matches/result/{matchId}", matchId) // POST 요청 URL
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"teamAScore\": 1, \"teamBScore\": 2, \"matchStatus\": \"CLOSED\", \"matchId\":" + matchId + "}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("경기 결과 기록"));
    }

    @Test
    public void testGetMatchesByDate() throws Exception {
        // Given
        LocalDate date = LocalDate.now();

        MatchByStadiumResponseDto matchByStadiumResponseDto = new MatchByStadiumResponseDto(
                1L,
                "Stadium A",
                "A great stadium",
                "Location A",
                "14:00",
                "16:00",
                MatchStatus.OPEN
        );

        List<MatchByStadiumResponseDto> matches = Collections.singletonList(matchByStadiumResponseDto);
        MatchesByDateResponseDto responseDto = new MatchesByDateResponseDto(matches);

        when(matchService.getMatchesByDate(date)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/matches")
                        .param("date", date.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("날짜별 매치 조회 성공"));
    }
}