package com.sparta.sportify.util.payment;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class KakaoPayApi {
    private final RestTemplate restTemplate;

    private static final String READY_URL = "https://open-api.kakaopay.com/online/v1/payment/ready";
    private static final String APPROVE_URL = "https://open-api.kakaopay.com/online/v1/payment/approve";
    private static final String CANCEL_URL = "https://open-api.kakaopay.com/online/v1/payment/cancel";

    public KakaoPayApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<HashMap> sendKakaoPayReadyRequest(String secretKey, Map<String, String> parameters) {
        // 요청 본문 생성
        HashMap<String, String> body = createReadyRequestBody(parameters);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        // 요청 엔티티 생성
        HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // API 호출
        return restTemplate.exchange(READY_URL, HttpMethod.POST, requestEntity, HashMap.class);
    }

    public ResponseEntity<HashMap> sendKakaoPayApproveRequest(String secretKey, Map<String, String> parameters) {
        // 요청 본문 생성
        HashMap<String, String> body = createApproveRequestBody(parameters);

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        // 요청 엔티티 생성
        HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // API 호출
        return restTemplate.exchange(APPROVE_URL, HttpMethod.POST, requestEntity, HashMap.class);
    }

    public ResponseEntity<HashMap> sendKakaoPayCancelRequest(String secretKey, Map<String, String> parameters) {
        HashMap<String, String> body = createCancelRequestBody(parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        // 요청 엔티티 생성
        HttpEntity<HashMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // API 호출
        return restTemplate.exchange(CANCEL_URL, HttpMethod.POST, requestEntity, HashMap.class);
    }

    /**
     * 요청 본문 생성 메서드
     *
     * @param parameters 필요한 요청 매개변수
     * @return 요청 본문 Map
     */
    private HashMap<String, String> createReadyRequestBody(Map<String, String> parameters) {
        HashMap<String, String> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("partner_order_id", "order_1234");
        body.put("partner_user_id", parameters.get("partner_user_id"));
        body.put("item_name", "캐쉬 충전");
        body.put("quantity", "1");
        body.put("total_amount", parameters.get("totalAmount"));
        body.put("tax_free_amount", "0");
        body.put("approval_url", "http://43.201.213.222:8080/api/cash/success");
        body.put("cancel_url", "http://43.201.213.222:8080/api/cash/cancel");
        body.put("fail_url", "http://43.201.213.222:8080/api/cash/fail");
        return body;
    }

    private HashMap<String, String> createApproveRequestBody(Map<String, String> parameters) {
        HashMap<String, String> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("tid", parameters.get("tid"));
        body.put("partner_order_id", "order_1234");
        body.put("partner_user_id", parameters.get("partner_user_id"));
        body.put("pg_token", parameters.get("pg_token"));
        return body;
    }

    private HashMap<String, String> createCancelRequestBody(Map<String, String> parameters) {
        HashMap<String, String> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("tid", parameters.get("tid"));
        body.put("cancel_amount", parameters.get("cancel_amount"));
        body.put("cancel_tax_free_amount", "0");
        return body;
    }
}
