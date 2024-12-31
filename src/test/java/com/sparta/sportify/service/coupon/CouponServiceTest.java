package com.sparta.sportify.service.coupon;

import com.sparta.sportify.dto.coupon.request.CouponCreateRequestDto;
import com.sparta.sportify.dto.coupon.response.CouponCreateResponseDto;
import com.sparta.sportify.entity.cashLog.CashLog;
import com.sparta.sportify.entity.cashLog.CashType;
import com.sparta.sportify.entity.coupon.Coupon;
import com.sparta.sportify.entity.coupon.CouponStatus;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.CashLogRepository;
import com.sparta.sportify.repository.CouponRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.CouponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CouponServiceTest {

    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CashLogRepository cashLogRepository;

    @Mock
    private UserRepository userRepository;

    private User user;
    private UserDetailsImpl authUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .id(1L)
                .email("test@example.com")
                .cash(0L)
                .build();
        authUser = new UserDetailsImpl(user.getEmail(), user.getRole(), user);
    }

    @Test
    @DisplayName("쿠폰 생성 성공 테스트")
    void createCoupon_success() {
        // given
        CouponCreateRequestDto requestDto = new CouponCreateRequestDto(
                "NEWYEAR2025",
                "New Year Coupon",
                1000L,
                LocalDate.now().plusDays(7),
                10000L
        );


        when(couponRepository.findByCode(requestDto.getCode())).thenReturn(Optional.empty());
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CouponCreateResponseDto response = couponService.createCoupon(requestDto);

        // then
        assertNotNull(response);
        assertEquals(requestDto.getCode(), response.getCode());
        verify(couponRepository, times(1)).save(any(Coupon.class));
    }

    @Test
    @DisplayName("쿠폰 생성 실패 테스트 (중복 코드)")
    void createCoupon_fail_duplicateCode() {
        // given
        CouponCreateRequestDto requestDto = new CouponCreateRequestDto(
                "NEWYEAR2025",
                "New Year Coupon",
                1000L,
                LocalDate.now().plusDays(7),
                10000L
        );

        when(couponRepository.findByCode(requestDto.getCode())).thenReturn(Optional.of(new Coupon()));

        // when & then
        CustomApiException exception = assertThrows(CustomApiException.class, () -> couponService.createCoupon(requestDto));
        assertEquals(ErrorCode.COUPON_ALREADY_EXISTS, exception.getErrorCode());
    }

    @Test
    @DisplayName("쿠폰 전체 조회 성공")
    void findAllCoupon_success() {
        // given
        PageRequest pageable = PageRequest.of(0, 10);
        Coupon coupon = Coupon.builder()
                .code("COUPON1")
                .name("Test Coupon")
                .price(100L)
                .count(100L)
                .expireDate(LocalDate.now().plusDays(1))
                .status(CouponStatus.AVAILABLE)
                .build();
        when(couponRepository.findAll(pageable)).thenReturn(new PageImpl<>(Collections.singletonList(coupon)));

        // when
        Slice<CouponCreateResponseDto> response = couponService.findAllCoupon(pageable);

        // then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("COUPON1", response.getContent().get(0).getCode());
    }


    @Test
    @DisplayName("사용자의 쿠폰 히스토리 조회 성공")
    void getUserCouponHistory_success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        CashLog cashLog = CashLog.builder()
                .id(1L)
                .user(user)
                .coupon(Coupon.builder()
                        .name("New Year Coupon")
                        .code("NEWYEAR2025")
                        .build())
                .price(1000L)
                .createAt(LocalDateTime.now())
                .type(CashType.COUPON)
                .build();
        when(cashLogRepository.findAllByUserIdWithCoupon(user.getId(), pageable)).thenReturn(new SliceImpl<>(List.of(cashLog)));

        // when
        var response = couponService.getUserCouponHistory(authUser, pageable);


        System.out.println(response.getContent().get(0).toString());

        // then
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals("New Year Coupon", response.getContent().get(0).getName());
    }

    @Test
    @DisplayName("쿠폰 사용 성공 테스트")
    void useCoupon_success() {
        // given
        String couponCode = "NEWYEAR2025";
        Coupon coupon = Coupon.builder()
                .id(1L)
                .code(couponCode)
                .name("New Year Coupon")
                .price(1000L)
                .count(10L)
                .status(CouponStatus.AVAILABLE)
                .build();
        when(couponRepository.findByCode(couponCode)).thenReturn(Optional.of(coupon));
        when(cashLogRepository.findByUserIdAndCouponId(user.getId(), coupon.getId())).thenReturn(Optional.empty());
        when(couponRepository.save(coupon)).thenReturn(coupon);
        when(cashLogRepository.save(any(CashLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(user)).thenReturn(user);

        // when
        var response = couponService.useCoupon(couponCode, authUser);

        // then
        assertNotNull(response);
        assertEquals(couponCode, response.getCouponCode());
        assertEquals(1000, response.getPrice());
        verify(couponRepository, times(1)).save(coupon);
        verify(cashLogRepository, times(1)).save(any(CashLog.class));
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("쿠폰을 찾을수 없음")
    void useCoupon_couponNotFound() {
        when(couponRepository.findByCode("COUPON1")).thenReturn(Optional.empty());

        CustomApiException exception = assertThrows(CustomApiException.class, () -> couponService.useCoupon("COUPON1", authUser));
        assertEquals(ErrorCode.COUPON_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("쿠폰 사용 실패 테스트 (이미 사용된 쿠폰)")
    void useCoupon_fail_alreadyUsed() {
        // given
        String couponCode = "NEWYEAR2025";
        Coupon coupon = Coupon.builder().id(1L).code(couponCode).build();
        when(couponRepository.findByCode(couponCode)).thenReturn(Optional.of(coupon));
        when(cashLogRepository.findByUserIdAndCouponId(user.getId(), coupon.getId())).thenReturn(Optional.of(new CashLog()));

        // when & then
        CustomApiException exception = assertThrows(CustomApiException.class, () -> couponService.useCoupon(couponCode, authUser));
        assertEquals(ErrorCode.COUPON_ALREADY_USED, exception.getErrorCode());
    }
}
