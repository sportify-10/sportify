package com.sparta.sportify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.dto.match.response.MatchesByDateResponseDto;
import com.sparta.sportify.service.MatchService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {
	private final MatchService matchService;

	@GetMapping
	public ResponseEntity<ApiResult<MatchesByDateResponseDto>> getMatchesByDate(
		// @RequestParam(defaultValue = "0") int page,
		// @RequestParam(defaultValue = "5") int size,
		// @RequestParam LocalDate date,
		// @AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		return ResponseEntity.ok(ApiResult.success("날짜별 매치 조회 성공", matchService.getMatchesByDate(/*page, size, date,  userDetails*/)));
	}
}
