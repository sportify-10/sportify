package com.sparta.sportify.service.cash;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashLogsResponseDto;
import com.sparta.sportify.dto.cash.response.CashResponseDto;
import com.sparta.sportify.dto.kakaoPay.request.KakaoPayApproveRequestDto;
import com.sparta.sportify.dto.kakaoPay.response.KakaoPayReadyResponseDto;
import com.sparta.sportify.entity.cashLog.CashLog;
import com.sparta.sportify.entity.cashLog.CashType;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.CashLogRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.CashService;
import com.sparta.sportify.service.KakaoPayService;
import com.sparta.sportify.util.payment.KakaoPayApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    private CashService cashService;

    private KakaoPayService kakaoPayService;

    @Mock
    private KakaoPayApi kakaoPayApi;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CashLogRepository cashLogRepository;

    private User user;
    private UserDetailsImpl userDetails;
    private CashRequestDto cashRequestDto;
    private String secretKey = "abc";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .id(1L)
                .active(true)
                .age(20L)
                .deletedAt(null)
                .email("test@example.com")
                .name("John Doe")
                .password("password123")
                .role(UserRole.USER)
                .cash(1000L) // 선택적 필드
                .build();
        cashRequestDto = new CashRequestDto(5000L);
        userDetails = new UserDetailsImpl(user.getName(), user.getRole(), user);

        kakaoPayService = new KakaoPayService(secretKey, kakaoPayApi);
        cashService = new CashService(cashLogRepository, userRepository, kakaoPayService);
    }

    @Test
    void testPrepareCashPayment() {
        // Given
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        CashRequestDto request = new CashRequestDto(10000L);
        KakaoPayReadyResponseDto kakaoResponse = new KakaoPayReadyResponseDto("tid123", "http://redirect.url");

        Map<String, String> parameters = Map.of("totalAmount", String.valueOf(request.getAmount()));

        HashMap<String, String> responseMap = new HashMap<>();
        responseMap.put("tid", "tid123");
        responseMap.put("next_redirect_pc_url", "http://redirect.url");

        ResponseEntity<HashMap> responseEntity = ResponseEntity.ok(responseMap);

        // Stubbing sendKakaoPayReadyRequest
        doReturn(responseEntity).when(kakaoPayApi).sendKakaoPayReadyRequest(eq(secretKey), eq(parameters));

        KakaoPayReadyResponseDto response = cashService.prepareCashPayment(userDetails, request);

        // Then
        assertNotNull(response);
        assertEquals("tid123", response.getTid());
        assertEquals("http://redirect.url", response.getNextRedirectUrl());

        verify(cashLogRepository, times(1)).save(argThat(cashLog ->
                cashLog.getPrice() == 10000 &&
                        "tid123".equals(cashLog.getTid()) &&
                        cashLog.getType() == CashType.PENDING &&
                        cashLog.getUser() == userDetails.getUser()
        ));

        verify(kakaoPayApi, times(1)).sendKakaoPayReadyRequest(eq(secretKey), eq(parameters));
    }

    @Test
    @DisplayName("캐시 결제 승인 성공")
    void testApproveCashPayment() {
        // Given
        KakaoPayApproveRequestDto requestDto = new KakaoPayApproveRequestDto("tid123", "pg_token_example", "user_1", 5000L);

        CashLog pendingCashLog = CashLog.builder()
                .id(1L)
                .price(5000L)
                .createAt(LocalDateTime.now().minusMinutes(10))
                .type(CashType.PENDING)
                .user(user)
                .tid("tid123")
                .build();

        when(cashLogRepository.findByUserIdAndTypeAndPrice(
                eq(user.getId()), eq(CashType.PENDING), eq(requestDto.getAmount())))
                .thenReturn(java.util.Optional.of(pendingCashLog));

        Map<String, String> parameters = Map.of(
                "tid", pendingCashLog.getTid(),
                "partner_user_id", requestDto.getUserId(),
                "pg_token", requestDto.getPgToken()
        );

        when(kakaoPayApi.sendKakaoPayApproveRequest(eq(secretKey), eq(parameters))).thenAnswer(invocation -> {
            // 실제 동작 없이 성공을 가정한 더미 응답 처리
            return ResponseEntity.ok(new HashMap<>());
        });

        CashLog updatedCashLog = CashLog.builder()
                .id(pendingCashLog.getId())
                .price(pendingCashLog.getPrice())
                .createAt(LocalDateTime.now())
                .type(CashType.CHARGE)
                .user(pendingCashLog.getUser())
                .tid(pendingCashLog.getTid())
                .build();

        when(cashLogRepository.save(any(CashLog.class))).thenReturn(updatedCashLog);

        // When
        CashResponseDto response = cashService.approveCashPayment(userDetails, requestDto);

        // Then
        assertNotNull(response);
        assertEquals(5000L, response.getChargeAmount());
        assertEquals(CashType.CHARGE, response.getType());
        assertEquals(user.getCash(), response.getBalance());

        verify(cashLogRepository, times(1)).findByUserIdAndTypeAndPrice(
                eq(user.getId()), eq(CashType.PENDING), eq(requestDto.getAmount()));

        verify(kakaoPayApi, times(1)).sendKakaoPayApproveRequest(eq(secretKey), eq(parameters));

        verify(cashLogRepository, times(1)).save(argThat(cashLog ->
                cashLog.getPrice() == 5000L &&
                        "tid123".equals(cashLog.getTid()) &&
                        cashLog.getType() == CashType.CHARGE &&
                        cashLog.getUser() == user
        ));

        verify(userRepository, times(1)).save(argThat(savedUser ->
                savedUser.getCash() == user.getCash()
        ));
    }

    @Test
    @DisplayName("캐시 환불 성공")
    void testCashRefund() {
        // Given
        user.setCash(10000L); // 초기 cash 설정
        CashRequestDto requestDto = new CashRequestDto(9000L); // 환불 요청 금액

        CashLog chargeCashLog = CashLog.builder()
                .id(1L)
                .price(9000L) // 환불 가능한 금액
                .createAt(LocalDateTime.now().minusMinutes(30))
                .type(CashType.CHARGE)
                .user(user)
                .tid("tid123")
                .build();

        when(cashLogRepository.findByUserIdAndTypeAndPrice(
                eq(user.getId()), eq(CashType.CHARGE), eq(requestDto.getAmount())))
                .thenReturn(java.util.Optional.of(chargeCashLog));

        Map<String, String> parameters = Map.of(
                "tid", chargeCashLog.getTid(),
                "cancel_amount", String.valueOf(requestDto.getAmount())
        );

        when(kakaoPayApi.sendKakaoPayCancelRequest(eq(secretKey), eq(parameters))).thenAnswer(invocation -> {
            // 실제 동작 없이 성공을 가정한 더미 응답 처리
            return ResponseEntity.ok(new HashMap<>());
        });

        CashLog refundCashLog = CashLog.builder()
                .id(chargeCashLog.getId())
                .price(-chargeCashLog.getPrice()) // 환불 금액 (음수)
                .createAt(LocalDateTime.now())
                .type(CashType.REFUND)
                .user(chargeCashLog.getUser())
                .tid(chargeCashLog.getTid())
                .build();

        when(cashLogRepository.save(any(CashLog.class))).thenReturn(refundCashLog);

        // When
        CashResponseDto response = cashService.CashRefund(userDetails, requestDto);

        // Then
        assertNotNull(response);
        assertEquals(-9000L, response.getChargeAmount());
        assertEquals(CashType.REFUND, response.getType());
        assertEquals(1000L, response.getBalance()); // 10000 - 9000 = 1000

        verify(cashLogRepository, times(1)).findByUserIdAndTypeAndPrice(
                eq(user.getId()), eq(CashType.CHARGE), eq(requestDto.getAmount()));

        verify(kakaoPayApi, times(1)).sendKakaoPayCancelRequest(eq(secretKey), eq(parameters));

        verify(cashLogRepository, times(1)).save(argThat(cashLog ->
                cashLog.getPrice() == -9000L &&
                        "tid123".equals(cashLog.getTid()) &&
                        cashLog.getType() == CashType.REFUND &&
                        cashLog.getUser() == user
        ));

        verify(userRepository, times(1)).save(argThat(savedUser ->
                savedUser.getCash() == 1000L // 최종 잔액 확인
        ));
    }


    @Test
    @DisplayName("캐시 조회 성공")
    void getCashLogs() {
        CashLog cashLog1 = CashLog.builder()
                .id(1L)
                .price(500L)
                .createAt(LocalDateTime.now())
                .type(CashType.CHARGE)
                .user(user)
                .build();

        CashLog cashLog2 = CashLog.builder()
                .id(2L)
                .price(300L)
                .createAt(LocalDateTime.now())
                .type(CashType.CHARGE)
                .user(user)
                .build();

        Page<CashLog> cashLogPage = new PageImpl<>(Arrays.asList(cashLog1, cashLog2));

        when(cashLogRepository.findAllByUserId(any(Long.class), any(Pageable.class))).thenReturn(cashLogPage);

        Page<CashLogsResponseDto> response = cashService.getCashLogs(userDetails, 1, 10);

        assertNotNull(response);
        assertEquals(2, response.getContent().size());

        CashLogsResponseDto responseDto1 = response.getContent().get(0);
        assertEquals(500L, responseDto1.getAmount());
        assertEquals(cashLog1.getCreateAt(), responseDto1.getCreateAt());
        assertEquals(cashLog1.getType(), responseDto1.getType());

        CashLogsResponseDto responseDto2 = response.getContent().get(1);
        assertEquals(300L, responseDto2.getAmount());
        assertEquals(cashLog2.getCreateAt(), responseDto2.getCreateAt());
        assertEquals(cashLog2.getType(), responseDto2.getType());
    }

    @Test
    @DisplayName("캐시 로그가 없을 때 예외 발생")
    void testGetCashLogsThrowsExceptionWhenEmpty() {
        // Given
        int page = 1;
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);

        when(cashLogRepository.findAllByUserId(eq(user.getId()), eq(pageable)))
                .thenReturn(Page.empty()); // 빈 결과 반환

        // When & Then
        CustomApiException exception = assertThrows(CustomApiException.class, () -> {
            cashService.getCashLogs(userDetails, page, size);
        });

        assertEquals(ErrorCode.CASH_LOG_NOT_FOUND, exception.getErrorCode());

        verify(cashLogRepository, times(1)).findAllByUserId(eq(user.getId()), eq(pageable));
    }


    @Test
    @DisplayName("PENDING 상태의 결제 로그 조회 성공")
    void testFindPendingCashLog() {
        // Given
        CashLog pendingCashLog = CashLog.builder()
                .id(1L)
                .price(5000L)
                .createAt(LocalDateTime.now().minusMinutes(10))
                .type(CashType.PENDING)
                .user(user)
                .tid("tid123")
                .build();

        when(cashLogRepository.findFirstByUserIdAndTypeOrderByCreateAtDesc(
                eq(user.getId()), eq(CashType.PENDING)))
                .thenReturn(java.util.Optional.of(pendingCashLog));

        // When
        CashLog result = cashService.findPendingCashLog(userDetails);

        // Then
        assertNotNull(result);
        assertEquals(pendingCashLog.getId(), result.getId());
        assertEquals(pendingCashLog.getPrice(), result.getPrice());
        assertEquals(pendingCashLog.getType(), result.getType());
        assertEquals(pendingCashLog.getUser(), result.getUser());
        assertEquals(pendingCashLog.getTid(), result.getTid());

        verify(cashLogRepository, times(1)).findFirstByUserIdAndTypeOrderByCreateAtDesc(
                eq(user.getId()), eq(CashType.PENDING));
    }
}