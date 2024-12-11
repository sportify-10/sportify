package com.sparta.sportify.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.CashService;
import com.sparta.sportify.util.api.ApiResult;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {
	private final CashService cashService;

	@PostMapping
	public ResponseEntity<ApiResult<CashResponseDto>> addCash(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody CashRequestDto cashRequestDto
	){
		return ResponseEntity.ok(ApiResult.success("캐시 충전 성공", cashService.addCash(userDetails, cashRequestDto)));
	}
}
