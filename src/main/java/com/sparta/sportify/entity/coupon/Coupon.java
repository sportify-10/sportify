package com.sparta.sportify.entity.coupon;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "coupons")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    private String name;

    private Long count;

    private LocalDate expireDate;

    private Long price;

    private CouponStatus status;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public void decrease() {
        validateStockCount();
        this.count -= 1;
    }

    public void validateStockCount() {
        if (this.count < 1 || this.status == CouponStatus.EXPIRED) {
            throw new IllegalArgumentException("만료된 쿠폰입니다.");
        }
    }
}
