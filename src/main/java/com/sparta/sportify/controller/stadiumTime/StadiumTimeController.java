package com.sparta.sportify.controller.stadiumTime;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.dto.stadiumTime.request.StadiumTimeCreateRequestDto;
import com.sparta.sportify.dto.stadiumTime.response.StadiumTimeCreateResponseDto;
import com.sparta.sportify.repository.StadiumTimeRepository;
import com.sparta.sportify.service.stadiumTimeService.StadiumTimeService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stadiumTime")
@RequiredArgsConstructor
public class StadiumTimeController {
	private final StadiumTimeRepository stadiumTimeRepository;
	private final StadiumTimeService stadiumTimeService;

	@PostMapping("/{stadiumId}")
	public ResponseEntity<ApiResult<StadiumTimeCreateResponseDto>> createStadiumTime(
		@PathVariable Long stadiumId,
		@RequestBody StadiumTimeCreateRequestDto stadiumTimeCreateRequestDto
	) {
		return ResponseEntity.ok(ApiResult.success("구장 시간 생성 성공", stadiumTimeService.createStadiumTime(stadiumId, stadiumTimeCreateRequestDto)));
	}
}
