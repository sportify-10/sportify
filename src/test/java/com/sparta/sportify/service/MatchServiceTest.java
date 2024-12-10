package com.sparta.sportify.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.RequestEntity.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.controller.MatchController;
import com.sparta.sportify.dto.match.MatchResultRequestDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.entity.Match;
import com.sparta.sportify.entity.MatchResult;
import com.sparta.sportify.entity.MatchStatus;
import com.sparta.sportify.repository.MatchRepository;
import com.sparta.sportify.repository.MatchResultRepository;

import jakarta.persistence.EntityNotFoundException;

class MatchServiceTest {
	@InjectMocks
	private MatchService matchService;

	@Mock
	private MatchResultRepository matchResultRepository;

	@Mock
	private MatchRepository matchRepository;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
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
}