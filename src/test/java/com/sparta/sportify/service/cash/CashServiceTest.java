package com.sparta.sportify.service.cash;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashLogsResponseDto;
import com.sparta.sportify.dto.kakaoPay.response.KakaoPayReadyResponseDto;
import com.sparta.sportify.entity.cashLog.CashLog;
import com.sparta.sportify.entity.cashLog.CashType;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles(value = {"dev"})
@TestPropertySource(properties = {"spring.config.location = classpath:application-dev.yml"})
class CashServiceTest {


    @InjectMocks
    private CashService cashService;

    @Mock
    private KakaoPayApi kakaoPayApi;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private KakaoPayService kakaoPayService;

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
        kakaoPayService = new KakaoPayService(kakaoPayApi);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
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

        // When
        when(kakaoPayService.preparePayment(userDetails, request)).thenCallRealMethod(); // Real call to preparePayment

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
}