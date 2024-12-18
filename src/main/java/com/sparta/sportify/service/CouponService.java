package com.sparta.sportify.service;

import com.sparta.sportify.dto.cash.response.CashLogCouponUseResponse;
import com.sparta.sportify.dto.coupon.request.CouponCreateRequestDto;
import com.sparta.sportify.dto.coupon.response.CouponCreateResponseDto;
import com.sparta.sportify.dto.coupon.response.CouponUserHistoryResponseDto;
import com.sparta.sportify.dto.reservation.response.ReservationFindResponseDto;
import com.sparta.sportify.entity.*;
import com.sparta.sportify.repository.CashLogRepository;
import com.sparta.sportify.repository.CouponRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CouponService {
    public final CouponRepository couponRepository;
    public final CashLogRepository cashLogRepository;

    @Transactional
    public CouponCreateResponseDto createCoupon(CouponCreateRequestDto couponCreateRequestDto) {
        Optional<Coupon> couponOp = couponRepository.findByCode(couponCreateRequestDto.getCode());
        if(couponOp.isPresent()) {
            throw new RuntimeException("쿠폰이 이미 존재합니다");
        }
        return new CouponCreateResponseDto(couponRepository.save(Coupon.builder()
                .code(couponCreateRequestDto.getCode())
                .name(couponCreateRequestDto.getName())
                .price(couponCreateRequestDto.getPrice())
                .count(couponCreateRequestDto.getCount())
                .expireDate(couponCreateRequestDto.getExpireDate())
                .status(CouponStatus.AVAILABLE)
                .build()));
    }
    @Transactional
    public Slice<CouponCreateResponseDto> findAllCoupon(Pageable pageable) {
        Slice<Coupon> coupons = couponRepository.findAll(pageable);
        return coupons.map(CouponCreateResponseDto::new);
    }

    @Transactional
    public Slice<CouponUserHistoryResponseDto> getUserCouponHistory(UserDetailsImpl authUser, Pageable pageable) {
        Slice<CashLog> cashLogSlice = cashLogRepository.findAllByUserIdWithCoupon(authUser.getUser().getId(),pageable);

        List<CouponUserHistoryResponseDto> couponHistory = cashLogSlice.getContent().stream()
                .map(cashLog -> new CouponUserHistoryResponseDto(
                        cashLog.getCoupon().getName(),
                        cashLog.getCoupon().getCode(),
                        cashLog.getCreateAt(),
                        cashLog.getPrice()
                ))
                .collect(Collectors.toList());


        return new SliceImpl<>(couponHistory, pageable, cashLogSlice.hasNext());
    }

    @Transactional
    public CashLogCouponUseResponse useCoupon(String code, UserDetailsImpl authUser) {
        Coupon coupon = couponRepository.findByCode(code).orElseThrow(()->{
            throw new RuntimeException("존재하지않는 쿠폰입니다.");
        });

        coupon.validateStockCount();

        CashLog cashLog = cashLogRepository.save(CashLog.builder()
                .user(authUser.getUser())
                .coupon(coupon)
                .type(CashType.COUPON)
                .price(coupon.getPrice())
                .build());

        return CashLogCouponUseResponse.builder()
                .couponCode(cashLog.getCoupon().getCode())
                .createAt(cashLog.getCreateAt())
                .price(cashLog.getPrice())
                .type(CashType.COUPON)
                .couponCode(cashLog.getCoupon().getCode())
                .build();
    }

}
