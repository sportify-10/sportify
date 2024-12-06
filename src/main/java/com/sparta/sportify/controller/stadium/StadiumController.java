package com.sparta.sportify.controller.stadium;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.dto.stadium.response.StadiumResponseDto;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.stadium.StadiumService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stadium")
@RequiredArgsConstructor
public class StadiumController {
	private final StadiumService stadiumService;

	@PostMapping
	public ResponseEntity<ApiResult<StadiumResponseDto>> createStadium(
		@RequestBody StadiumCreateRequestDto stadiumCreateRequestDto,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		return ResponseEntity.ok(ApiResult.success("구장 생성 성공", stadiumService.createStadium(stadiumCreateRequestDto, userDetails)));
	}

	@GetMapping
	public ResponseEntity<ApiResult<List<StadiumResponseDto>>> getStadiums(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "5") int size
	) {
		return ResponseEntity.ok(ApiResult.success("구장 조회 성공",stadiumService.getStadiums(userDetails, page, size)));
	}

	@PatchMapping("/{stadiumId}")
	public ResponseEntity<ApiResult<StadiumResponseDto>> updateStadium(
		@PathVariable Long stadiumId,
		@RequestBody StadiumUpdateRequestDto stadiumUpdateRequestDto,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		return ResponseEntity.ok(ApiResult.success("구장 수정 성공",stadiumService.updateStadium(stadiumId, stadiumUpdateRequestDto, userDetails)));
	}

	@DeleteMapping("/{stadiumId}")
	public ResponseEntity<ApiResult<StadiumResponseDto>> deleteStadium(
		@PathVariable Long stadiumId,
		@AuthenticationPrincipal UserDetailsImpl userDetails
	) {
		return ResponseEntity.ok(ApiResult.success("구장 삭제 성공",stadiumService.deleteStadium(stadiumId, userDetails)));
	}
}
