package com.sparta.sportify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.dto.match.MatchResultRequestDto;
import com.sparta.sportify.dto.match.MatchResultResponseDto;
import com.sparta.sportify.service.MatchService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

	private final MatchService matchService;

	@PostMapping("/result/{matchId}")
	public ResponseEntity<ApiResult<MatchResultResponseDto>> createMatchResult(
		@Validated @RequestBody MatchResultRequestDto requestDto) {
		MatchResultResponseDto responseDto = matchService.createMatchResult(requestDto);
		return ResponseEntity.ok(ApiResult.success(responseDto));
	}
}