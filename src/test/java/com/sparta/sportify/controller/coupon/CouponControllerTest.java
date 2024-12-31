package com.sparta.sportify.controller.coupon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.sportify.dto.cash.response.CashLogCouponUseResponse;
import com.sparta.sportify.dto.coupon.request.CouponCreateRequestDto;
import com.sparta.sportify.dto.coupon.request.CouponUseRequestDto;
import com.sparta.sportify.dto.coupon.response.CouponCreateResponseDto;
import com.sparta.sportify.dto.coupon.response.CouponUserHistoryResponseDto;
import com.sparta.sportify.entity.cashLog.CashType;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CouponControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponService couponService;

    @Mock
    private UserDetailsImpl authUser;

    private CouponCreateRequestDto couponCreateRequestDto;

    private CouponUseRequestDto couponUseRequestDto;

    @BeforeEach
    void setUp() {
        couponCreateRequestDto = new CouponCreateRequestDto(
                "CODE123",
                "Sample Coupon",
                100L,
                null,
                5000L
        );
        couponUseRequestDto = new CouponUseRequestDto();
        couponUseRequestDto.setCode("CODE123");
    }


    @Test
    @WithMockUser(username = "adminUser", roles = "ADMIN")
    void testCreateCoupon() throws Exception {
        // Given
        CouponCreateResponseDto couponCreateResponseDto = new CouponCreateResponseDto(1L, "CODE123", "Test Coupon", 100L, LocalDate.now().plusDays(30), 1000L);
        when(couponService.createCoupon(any(CouponCreateRequestDto.class))).thenReturn(couponCreateResponseDto);

        // When & Then
        mockMvc.perform(post("/api/admin/coupon")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(couponCreateRequestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("쿠폰 생성 성공"))
                .andExpect(jsonPath("$.data.code").value("CODE123"));
    }

    @Test
    @WithMockUser(username = "adminUser", roles = "ADMIN")
    void testAdminCouponList() throws Exception {
        // Given
        Slice<CouponCreateResponseDto> couponList = new SliceImpl<>(List.of(new CouponCreateResponseDto(1L, "CODE123", "Test Coupon", 100L, LocalDate.now().plusDays(30), 1000L)));
        when(couponService.findAllCoupon(any(Pageable.class))).thenReturn(couponList);

        // When & Then
        mockMvc.perform(get("/api/admin/coupon")
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("쿠폰 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].code").value("CODE123"));
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testUserCouponList() throws Exception {
        // Given
        Slice<CouponUserHistoryResponseDto> userCouponHistory = new SliceImpl<>(List.of(new CouponUserHistoryResponseDto(
                "Sample Coupon",
                "CODE123",
                LocalDateTime.now(),
                5000L
        )));
        when(couponService.getUserCouponHistory(any(), any())).thenReturn(userCouponHistory);

        // When & Then
        mockMvc.perform(get("/api/coupon")
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저 쿠폰 조회 성공"))
                .andExpect(jsonPath("$.data.content[0].code").value("CODE123"));  // 실제 반환되는 data 값 확인

    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void testUseCoupon() throws Exception {

        CashLogCouponUseResponse cashLogCouponUseResponse = CashLogCouponUseResponse.builder()
                .price(5000L)
                .createAt(LocalDateTime.now())
                .type(CashType.COUPON)
                .couponCode("CODE123")
                .build();
        when(couponService.useCoupon(any(), any())).thenReturn(cashLogCouponUseResponse);

        mockMvc.perform(post("/api/coupon")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(couponUseRequestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("유저 쿠폰 사용 성공"))
                .andExpect(jsonPath("$.data.couponCode").value("CODE123"));
    }
}


