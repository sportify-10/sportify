package com.sparta.sportify.controller;

import com.sparta.sportify.dto.cash.response.CashLogCouponUseResponse;
import com.sparta.sportify.dto.coupon.request.CouponCreateRequestDto;
import com.sparta.sportify.dto.coupon.request.CouponUseRequestDto;
import com.sparta.sportify.dto.coupon.response.CouponCreateResponseDto;
import com.sparta.sportify.dto.coupon.response.CouponUserHistoryResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.CouponService;
import com.sparta.sportify.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CouponController {
    private final CouponService couponService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/coupon")
    public ResponseEntity<ApiResult<CouponCreateResponseDto>> createCoupon(
            @AuthenticationPrincipal UserDetailsImpl authUser,
            @RequestBody CouponCreateRequestDto couponCreateRequestDto
    ) {
        return new ResponseEntity<>(
                ApiResult.success(
                        "쿠폰 생성 성공",
                        couponService.createCoupon(couponCreateRequestDto)
                ),
                HttpStatus.OK
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/coupon")
    public ResponseEntity<ApiResult<Slice<CouponCreateResponseDto>>> adminCouponList(
            @AuthenticationPrincipal UserDetailsImpl authUser,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(
                ApiResult.success(
                        "쿠폰 조회 성공",
                        couponService.findAllCoupon(pageable)
                ),
                HttpStatus.OK
        );
    }
    @GetMapping("/coupon")
    public ResponseEntity<ApiResult<Slice<CouponUserHistoryResponseDto>>> userCouponList(
            @AuthenticationPrincipal UserDetailsImpl authUser,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new ResponseEntity<>(
                ApiResult.success(
                        "유저 쿠폰 조회 성공",
                        couponService.getUserCouponHistory(authUser,pageable)
                ),
                HttpStatus.OK
        );
    }

    @PostMapping("/coupon")
    public ResponseEntity<ApiResult<CashLogCouponUseResponse>> useCoupon(
            @AuthenticationPrincipal UserDetailsImpl authUser,
            @RequestBody CouponUseRequestDto couponUseRequestDto
    ) {
        return new ResponseEntity<>(
                ApiResult.success(
                        "유저 쿠폰 사용 성공",
                        couponService.useCoupon(couponUseRequestDto.getCode(), authUser)
                ),
                HttpStatus.OK
        );
    }
}
