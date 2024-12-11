package com.sparta.sportify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.dto.stadiumTime.request.StadiumTimeRequestDto;
import com.sparta.sportify.dto.stadiumTime.response.StadiumTimeResponseDto;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.StadiumTimeService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stadiumTime")
@RequiredArgsConstructor
public class StadiumTimeController {
	private final StadiumTimeRepository stadiumTimeRepository;
	private final StadiumTimeService stadiumTimeService;

	@PostMapping("/{stadiumId}")
	public ResponseEntity<ApiResult<StadiumTimeResponseDto>> createStadiumTime(
		@PathVariable Long stadiumId,
		@RequestBody StadiumTimeRequestDto stadiumTimeRequestDto
	) {
		return ResponseEntity.ok(ApiResult.success("구장 시간 생성 성공", stadiumTimeService.createStadiumTime(stadiumId,
			stadiumTimeRequestDto)));
	}

	@PatchMapping("/{stadiumTimeId}")
	public ResponseEntity<ApiResult<StadiumTimeResponseDto>> updateStadiumTime(
		@PathVariable Long stadiumTimeId,
		@RequestBody StadiumTimeRequestDto stadiumTimeRequestDto,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		return ResponseEntity.ok(ApiResult.success("구장 시간 수정 성공",stadiumTimeService.updateStadiumTime(stadiumTimeId, stadiumTimeRequestDto, userDetails)));
	}
}
