package com.sparta.sportify.service;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashLogsResponseDto;
import com.sparta.sportify.dto.cash.response.CashResponseDto;
import com.sparta.sportify.dto.kakaoPay.request.KakaoPayApproveRequestDto;
import com.sparta.sportify.dto.kakaoPay.response.KakaoPayReadyResponseDto;
import com.sparta.sportify.entity.CashLog;
import com.sparta.sportify.entity.CashType;
import com.sparta.sportify.repository.CashLogRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CashService {

	private final CashLogRepository cashLogRepository;
	private final UserRepository userRepository;
	private final KakaoPayService kakaoPayService;


	/**
	 * 결제 준비 요청 (결제 URL 반환)
	 */
	public KakaoPayReadyResponseDto prepareCashPayment(UserDetailsImpl userDetails, CashRequestDto request) {
		return kakaoPayService.preparePayment(userDetails, request);
	}

	public CashResponseDto approveCashPayment(UserDetailsImpl userDetails, KakaoPayApproveRequestDto request) {
		kakaoPayService.approvePayment(request);

		CashLog cashLog = cashLogRepository.save(
				CashLog.builder()
						.price(request.getAmount())
						.createAt(LocalDateTime.now())
						.type(CashType.CHARGE)
						.user(userDetails.getUser())
						.build());

		userDetails.getUser().addCash(request.getAmount());
		userRepository.save(userDetails.getUser());

		return new CashResponseDto(cashLog);
	}

	public CashResponseDto CashRefund(UserDetailsImpl userDetails, CashRequestDto request, String tid) {
		kakaoPayService.refundPayment(request, tid);
		CashLog cashLog = cashLogRepository.save(
				CashLog.builder()
						.price(-(request.getAmount()))
						.createAt(LocalDateTime.now())
						.type(CashType.REFUND)
						.user(userDetails.getUser())
						.build());
		userDetails.getUser().subCash(request.getAmount());
		userRepository.save(userDetails.getUser());

		return new CashResponseDto(cashLog);
	}

	public Page<CashLogsResponseDto> getCashLogs(UserDetailsImpl userDetails, int page, int size) {
		Pageable pageable = PageRequest.of(page-1, size);
		Page<CashLog> cashLogs = cashLogRepository.findAllByUserId(userDetails.getUser().getId(), pageable);

		if(cashLogs.isEmpty()) {
			throw new IllegalArgumentException("캐시 사용 내역이 없습니다");
		}

		return cashLogs.map(cashLog -> new CashLogsResponseDto(
			cashLog.getPrice(),
			cashLog.getCreateAt(),
			cashLog.getType()
		));
	}

}
