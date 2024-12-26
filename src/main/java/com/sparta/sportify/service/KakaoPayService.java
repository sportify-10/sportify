package com.sparta.sportify.service;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashResponseDto;
import com.sparta.sportify.dto.kakaoPay.request.KakaoPayApproveRequestDto;
import com.sparta.sportify.dto.kakaoPay.response.KakaoPayReadyResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoPayService {


    @Value("${kakao.pay.secret-key-dev}")
    private String secretKeyDev;

    private final RestTemplate restTemplate;

    public KakaoPayReadyResponseDto preparePayment(UserDetailsImpl userDetails, CashRequestDto cashRequestDto) {
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";

        HashMap<String, String> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("partner_order_id", "order_1234");
        body.put("partner_user_id", "1"/*userDetails.getUser().getId().toString()*/);
        body.put("item_name", "캐쉬 충전");
        body.put("quantity", "1");
        body.put("total_amount", String.valueOf(cashRequestDto.getAmount()));
        body.put("tax_free_amount", "0");
        body.put("approval_url", "http://localhost:8080/api/cash/success");
        body.put("cancel_url", "http://localhost:8080/api/cash/cancel");
        body.put("fail_url", "http://localhost:8080/api/cash/fail");

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKeyDev);

        HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // API 호출
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        // 응답 처리
        Map<String, String> responseBody = response.getBody();
        KakaoPayReadyResponseDto responseDto = new KakaoPayReadyResponseDto();
        responseDto.setTid(responseBody.get("tid"));
        responseDto.setNextRedirectUrl(responseBody.get("next_redirect_pc_url"));

        return responseDto;
    }

    public void approvePayment(KakaoPayApproveRequestDto requestDto, String tid) {
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";

        // 요청 데이터
        HashMap<String, String> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("tid", tid);
        body.put("partner_order_id", "order_1234");
        body.put("partner_user_id", requestDto.getUserId());
        body.put("pg_token", requestDto.getPgToken());


        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKeyDev);

        HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // API 호출
        restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
    }

    public CashResponseDto refundPayment(CashRequestDto requestDto, String tid) {
        String url = "https://open-api.kakaopay.com/online/v1/payment/cancel";

        HashMap<String, String> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("tid", tid);
        body.put("cancel_amount", requestDto.getAmount().toString());
        body.put("cancel_tax_free_amount", "0");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKeyDev);

        HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);

        CashResponseDto responseDto = new CashResponseDto();

        return responseDto;
    }
}
