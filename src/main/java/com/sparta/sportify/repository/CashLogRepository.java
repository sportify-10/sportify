package com.sparta.sportify.repository;

import com.sparta.sportify.entity.cashLog.CashLog;
import com.sparta.sportify.entity.cashLog.CashType;
import com.sparta.sportify.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CashLogRepository extends JpaRepository<CashLog, Long> {
    Page<CashLog> findAllByUserId(Long userId, Pageable pageable);

    @Query("SELECT cl FROM CashLog cl " +
            "JOIN FETCH cl.user u " +
            "JOIN FETCH cl.coupon c " +
            "WHERE u.id = :userId AND cl.coupon IS NOT NULL")
    Slice<CashLog> findAllByUserIdWithCoupon(@Param("userId") Long userId, Pageable pageable);

    Optional<CashLog> findByUserIdAndCouponId(Long userId, Long couponId);

    Optional<CashLog> findByUserIdAndTypeAndPrice(Long userId, CashType type, Long price);

    @Query("SELECT c FROM CashLog c WHERE c.user.id = :userId AND c.type = :type ORDER BY c.createAt DESC")
    Optional<CashLog> findFirstByUserIdAndTypeOrderByCreateAtDesc(@Param("userId") Long userId, @Param("type") CashType cashType);

    boolean existsByUserAndType(User user, CashType type);
}
