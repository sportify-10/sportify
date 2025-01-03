package com.sparta.sportify.controller.cash;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashLogsResponseDto;
import com.sparta.sportify.dto.cash.response.CashResponseDto;
import com.sparta.sportify.dto.kakaoPay.response.KakaoPayReadyResponseDto;
import com.sparta.sportify.entity.cashLog.CashLog;
import com.sparta.sportify.entity.cashLog.CashType;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.user.UserRole;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.CashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CashControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CashService cashService;
    @Mock
    private UserDetailsImpl userDetails;
    private User user;

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
                .cash(1000L)
                .build();
        userDetails = new UserDetailsImpl(user.getName(), user.getRole(), user);
    }

    @Test
    @WithMockUser(username = "testUser", roles = "USER")
    void testChargeCash() throws Exception {
        // Given
        CashRequestDto cashRequestDto = new CashRequestDto(10000L);
        KakaoPayReadyResponseDto responseDto = new KakaoPayReadyResponseDto("T1234567890", "https://kakaopay.payment.url");

        // Mocking service behavior
        when(cashService.prepareCashPayment(any(), any())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/cash/charge")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(cashRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캐쉬 충전 요청")) // 메시지 검증
                .andExpect(jsonPath("$.data.tid").value("T1234567890")) // 결제 고유 번호 검증
                .andExpect(jsonPath("$.data.nextRedirectUrl").value("https://kakaopay.payment.url")); // 결제 URL 검증

    }

    @Test
    void testApproveCash() throws Exception {
        // Given
        String pgToken = "pg_token_example";
        Long userId = 1L;

        // Mock UserDetailsImpl
        UserDetailsImpl mockUserDetails = new UserDetailsImpl("testUser", user.getRole(), user);

        // Mock CashLog
        CashLog mockCashLog = CashLog.builder()
                .user(User.builder()
                        .id(userId)
                        .cash(50000L)
                        .build())
                .tid("T1234567890")
                .price(10000L)
                .type(CashType.CHARGE)
                .createAt(LocalDateTime.now())
                .build();

        // Mock CashResponseDto
        CashResponseDto responseDto = new CashResponseDto(mockCashLog);

        // Set SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())
        );

        // Mocking service behavior
        when(cashService.findPendingCashLog(any(UserDetailsImpl.class))).thenReturn(mockCashLog);
        when(cashService.approveCashPayment(any(), any()))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/cash/success")
                        .param("pg_token", pgToken)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캐쉬 충전 성공")) // 메시지 검증
                .andExpect(jsonPath("$.data.userId").value(userId)) // 사용자 ID 검증
                .andExpect(jsonPath("$.data.balance").value(50000L)) // 잔액 검증
                .andExpect(jsonPath("$.data.type").value("CHARGE")) // 캐시 타입 검증
                .andExpect(jsonPath("$.data.chargeAmount").value(10000L));  // 결과 메시지 검증
    }

    @Test
    void testCancelCash() throws Exception {
        // Given
        Long userId = 1L;
        Long refundAmount = 5000L;

        // Mock UserDetailsImpl
        UserDetailsImpl mockUserDetails = new UserDetailsImpl("testUser", user.getRole(), user);

        // Mock CashRequestDto
        CashRequestDto refundRequestDto = new CashRequestDto(refundAmount);

        // Mock CashResponseDto
        CashResponseDto responseDto = new CashResponseDto();
        responseDto.setUserId(userId);
        responseDto.setBalance(45000L); // 환불 후 잔액
        responseDto.setCreateAt(LocalDateTime.now());
        responseDto.setType(CashType.REFUND);
        responseDto.setChargeAmount(refundAmount);

        // Set SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())
        );

        // Mocking service behavior
        when(cashService.CashRefund(any(), any())).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/cash/cancel")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(refundRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캐쉬 환불 완료")) // 성공 메시지 검증
                .andExpect(jsonPath("$.data.userId").value(userId)) // 사용자 ID 검증
                .andExpect(jsonPath("$.data.balance").value(45000L)) // 환불 후 잔액 검증
                .andExpect(jsonPath("$.data.type").value("REFUND")) // 캐시 타입 검증
                .andExpect(jsonPath("$.data.chargeAmount").value(refundAmount)); // 환불 금액 검증
    }

    @Test
    void testGetCashLogs() throws Exception {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 5;

        // Mock UserDetailsImpl
        UserDetailsImpl mockUserDetails = new UserDetailsImpl("testUser", user.getRole(), user);

        // Mock Page<CashLogsResponseDto>
        List<CashLogsResponseDto> cashLogs = List.of(
                new CashLogsResponseDto(10000L, LocalDateTime.now(), CashType.CHARGE),
                new CashLogsResponseDto(90000L, LocalDateTime.now(), CashType.CHARGE)
        );
        Page<CashLogsResponseDto> cashLogsPage = new PageImpl<>(cashLogs, PageRequest.of(page, size), cashLogs.size());

        // Set SecurityContext
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUserDetails, null, mockUserDetails.getAuthorities())
        );

        // Mocking service behavior
        when(cashService.getCashLogs(eq(userDetails), eq(page + 1), eq(size))).thenReturn(cashLogsPage);

        // When & Then
        mockMvc.perform(get("/api/cash")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("캐시 로그 조회 성공"));// 두 번째 로그의 잔액 검증

    }
}
