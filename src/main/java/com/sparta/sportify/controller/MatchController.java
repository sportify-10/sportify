package com.sparta.sportify.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.dto.match.MatchResponseDto;
import com.sparta.sportify.service.MatchService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matches")
public class MatchController {

	@Autowired
	private MatchService matchService;

	//매치 단건 조회
	@GetMapping("/{stadiumTimeId}")
	public ResponseEntity<ApiResult<MatchResponseDto>> getMatch(@PathVariable Long stadiumTimeId) {
		MatchResponseDto matchResponseDto = matchService.getMatchById(stadiumTimeId);
		return ResponseEntity.ok(ApiResult.success(matchResponseDto));
	}
}