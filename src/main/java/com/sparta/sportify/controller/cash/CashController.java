package com.sparta.sportify.controller.cash;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashLogsResponseDto;
import com.sparta.sportify.dto.cash.response.CashResponseDto;
import com.sparta.sportify.dto.kakaoPay.request.KakaoPayApproveRequestDto;
import com.sparta.sportify.dto.kakaoPay.response.KakaoPayReadyResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.CashService;
import com.sparta.sportify.util.api.ApiResult;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {
	private final CashService cashService;

	/**
	 * 캐시 충전 요청 (결제 준비)
	 * 사용자가 충전 금액을 요청하면, 카카오페이 결제 준비 API를 호출하여 결제 페이지 URL을 반환합니다.
	 */
	@PostMapping("/charge")
	public ResponseEntity<ApiResult<KakaoPayReadyResponseDto>> chargeCash(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestBody CashRequestDto cashRequestDto,
			HttpSession session) {

		// 결제 준비 요청
		KakaoPayReadyResponseDto responseDto = cashService.prepareCashPayment(userDetails, cashRequestDto);

		// 세션에 tid와 amount 저장(결제 완료 시 승인 요청에 사용)
		session.setAttribute("tid", responseDto.getTid());
		session.setAttribute("amount", cashRequestDto.getAmount());

		// 결제 페이지 URL 반환
		return ResponseEntity.ok(ApiResult.success("캐쉬 충전 요청", responseDto));
	}

	/**
	 * 결제 승인 처리
	 * 사용자가 카카오페이 결제페이지에서 결제를 완료하면 pg_token이 함께 approval_url로 리다이렉트됩니다.
	 * 여기서 pg_token을 이용해 결제 승인 API를 호출하고, 캐시를 충전합니다.
	 */
	@GetMapping("/success")
	public ResponseEntity<ApiResult<CashResponseDto>> approveCash(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestParam("pg_token") String pgToken,
			HttpSession session) {

		// 세션에서 tid와 amount 가져오기
		String tid = (String) session.getAttribute("tid");
		Long amount = (Long) session.getAttribute("amount");

		KakaoPayApproveRequestDto approveRequestDto = KakaoPayApproveRequestDto.builder()
				.tid(tid)
				.pgToken(pgToken)
				.amount(amount)
				.userId(userDetails.getUser().getId().toString())
				.build();

		// 결제 승인 및 캐시 충전
		CashResponseDto responseDto = cashService.approveCashPayment(userDetails, approveRequestDto);

		// 세션 정보 정리
		session.removeAttribute("amount");

		return ResponseEntity.ok(ApiResult.success("캐쉬 충전 성공", responseDto));
	}

	@PostMapping("cancel")
	public ResponseEntity<ApiResult<CashResponseDto>> cancelCash(
			@AuthenticationPrincipal UserDetailsImpl userDetails,
			@RequestBody CashRequestDto refundRequestDto,
			HttpSession session) {


		String tid = (String) session.getAttribute("tid");

		CashResponseDto responseDto = cashService.CashRefund(userDetails, refundRequestDto, tid);

		session.removeAttribute("tid");

		return ResponseEntity.ok(ApiResult.success("캐쉬 환불 완료", responseDto));
	}

	@GetMapping
	public ResponseEntity<ApiResult<Page<CashLogsResponseDto>>> getCashLogs(
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "5") int size
	) {
		return ResponseEntity.ok(ApiResult.success("캐시 로그 조회 성공",cashService.getCashLogs(userDetails, page, size)));
	}
}
