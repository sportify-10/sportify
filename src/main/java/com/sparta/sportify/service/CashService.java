package com.sparta.sportify.service;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashLogsResponseDto;
import com.sparta.sportify.dto.cash.response.CashResponseDto;
import com.sparta.sportify.dto.kakaoPay.request.KakaoPayApproveRequestDto;
import com.sparta.sportify.dto.kakaoPay.response.KakaoPayReadyResponseDto;
import com.sparta.sportify.entity.cashLog.CashLog;
import com.sparta.sportify.entity.cashLog.CashType;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
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

        KakaoPayReadyResponseDto responseDto = kakaoPayService.preparePayment(userDetails, request);
        boolean isAlreadyPending = cashLogRepository.existsByUserAndType(userDetails.getUser(), CashType.PENDING);
        if (isAlreadyPending) {
            throw new CustomApiException(ErrorCode.ALREADY_PENDING);
        }

        CashLog cashLog = CashLog.builder()
                .price(request.getAmount()) // 충전 금액
                .tid(responseDto.getTid()) // 카카오페이 TID
                .createAt(LocalDateTime.now())
                .type(CashType.PENDING) // CHARGE 타입으로 설정
                .user(userDetails.getUser()) // 현재 사용자
                .build();
        cashLogRepository.save(cashLog);

        return responseDto;
    }

    public CashResponseDto approveCashPayment(UserDetailsImpl userDetails, KakaoPayApproveRequestDto request) {
        CashLog existingCashLog = cashLogRepository.findByUserIdAndTypeAndPrice(
                        userDetails.getUser().getId(),
                        CashType.PENDING, // 승인 가능한 로그는 CHARGE 타입
                        request.getAmount()) // 요청 금액과 일치하는 로그 조회
                .orElseThrow(() -> new CustomApiException(ErrorCode.CHARGE_LOG_NOT_FOUND));
        String tid = existingCashLog.getTid();
        request.setUserId(String.valueOf(userDetails.getUser().getId()));
        kakaoPayService.approvePayment(request, tid);

        CashLog updatedCashLog = CashLog.builder()
                .id(existingCashLog.getId()) // 기존 로그의 ID 유지
                .price(existingCashLog.getPrice()) // 기존 금액 유지
                .createAt(LocalDateTime.now()) // 승인 시간 갱신
                .type(CashType.CHARGE) // 승인 상태로 변경
                .user(existingCashLog.getUser()) // 사용자 정보 유지
                .tid(existingCashLog.getTid()) // 기존 TID 유지
                .build();
        cashLogRepository.save(updatedCashLog);
        userDetails.getUser().addCash(existingCashLog.getPrice());
        userRepository.save(userDetails.getUser());

        return new CashResponseDto(updatedCashLog);
    }

    public CashResponseDto CashRefund(UserDetailsImpl userDetails, CashRequestDto request) {
        CashLog existingCashLog = cashLogRepository.findFirstByUserIdAndTypeAndPriceOrderByCreateAtDesc(
                        userDetails.getUser().getId(),
                        CashType.CHARGE, // 환불은 CHARGE 타입에서만 가능
                        request.getAmount()) // 요청 금액과 일치하는 로그 조회
                .orElseThrow(() -> new CustomApiException(ErrorCode.REFUND_LOG_NOT_FOUND));
        String tid = existingCashLog.getTid();
        kakaoPayService.refundPayment(request, tid);
        CashLog refundCashLog = CashLog.builder()
                .id(existingCashLog.getId()) // 기존 로그의 ID 유지
                .price(-existingCashLog.getPrice()) // 환불 금액 (음수로 설정)
                .createAt(LocalDateTime.now()) // 환불 시간 갱신
                .type(CashType.REFUND) // 환불 상태로 변경
                .user(existingCashLog.getUser()) // 사용자 정보 유지
                .tid(existingCashLog.getTid()) // 기존 TID 유지
                .build();
        cashLogRepository.save(refundCashLog);
        userDetails.getUser().subCash(existingCashLog.getPrice());
        userRepository.save(userDetails.getUser());

        return new CashResponseDto(refundCashLog);
    }

    public Page<CashLogsResponseDto> getCashLogs(UserDetailsImpl userDetails, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<CashLog> cashLogs = cashLogRepository.findAllByUserId(userDetails.getUser().getId(), pageable);

        if (cashLogs.isEmpty()) {
            throw new CustomApiException(ErrorCode.CASH_LOG_NOT_FOUND);
        }

        return cashLogs.map(cashLog -> new CashLogsResponseDto(
                cashLog.getPrice(),
                cashLog.getCreateAt(),
                cashLog.getType()
        ));
    }

    public CashLog findPendingCashLog(UserDetailsImpl userDetails) {
        return cashLogRepository.findFirstByUserIdAndTypeOrderByCreateAtDesc(
                        userDetails.getUser().getId(),
                        CashType.PENDING) // PENDING 상태의 로그 조회
                .orElseThrow(() -> new IllegalArgumentException("승인 대기 중인 결제 로그가 없습니다."));
    }
}