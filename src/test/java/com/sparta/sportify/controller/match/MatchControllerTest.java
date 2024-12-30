package com.sparta.sportify.controller.match;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.sparta.sportify.dto.match.MatchDetailResponseDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.entity.matchResult.MatchStatus;
import com.sparta.sportify.service.MatchService;

@SpringBootTest
@AutoConfigureMockMvc
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
		// 필요한 데이터로 responseDto를 설정
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

		// MatchResultResponseDto 객체 생성
		MatchResultResponseDto responseDto = new MatchResultResponseDto(
			matchId,
			1,
			2,
			MatchStatus.CLOSED, // MatchStatus 설정
			LocalDate.now() // 현재 날짜 설정
		);

		when(matchService.getMatchResult(matchId)).thenReturn(responseDto);

		// When & Then
		mockMvc.perform(get("/api/matches/result/{matchId}", matchId) // GET 요청 URL
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.message").value("경기 결과 조회"));
	}

}