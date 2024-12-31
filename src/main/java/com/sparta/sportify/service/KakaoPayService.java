package com.sparta.sportify.service;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashResponseDto;
import com.sparta.sportify.dto.kakaoPay.request.KakaoPayApproveRequestDto;
import com.sparta.sportify.dto.kakaoPay.response.KakaoPayReadyResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.util.payment.KakaoPayApi;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoPayService {

    @Value("${kakao.pay.secret-key-dev}")
    private String secretKeyDev;

    private final KakaoPayApi kakaoPayApi;

    public KakaoPayReadyResponseDto preparePayment(UserDetailsImpl authUser, CashRequestDto cashRequestDto) {
        Map<String, String> parameters = Map.of(
                "totalAmount", String.valueOf(cashRequestDto.getAmount())
        );

        ResponseEntity<HashMap> response = kakaoPayApi.sendKakaoPayReadyRequest(secretKeyDev, parameters);

        // 응답 처리
        Map<String, String> responseBody = response.getBody();
        KakaoPayReadyResponseDto responseDto = new KakaoPayReadyResponseDto(
                responseBody.get("tid"),
                responseBody.get("next_redirect_pc_url"));

        return responseDto;
    }

    public void approvePayment(KakaoPayApproveRequestDto requestDto, String tid) {
        Map<String, String> parameters = Map.of(
                "tid", tid,
                "partner_user_id", requestDto.getUserId(),
                "pg_token", requestDto.getPgToken()
        );

        kakaoPayApi.sendKakaoPayApproveRequest(secretKeyDev, parameters);
    }

    public CashResponseDto refundPayment(CashRequestDto requestDto, String tid) {
        Map<String, String> parameters = Map.of(
                "tid", tid,
                "cancel_amount", String.valueOf(requestDto.getAmount())
        );

        kakaoPayApi.sendKakaoPayCancelRequest(secretKeyDev, parameters);

        CashResponseDto responseDto = new CashResponseDto();

        return responseDto;
    }
}
