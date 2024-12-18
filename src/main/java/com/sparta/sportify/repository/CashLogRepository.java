package com.sparta.sportify.repository;

import java.util.List;

import com.sparta.sportify.entity.CashLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CashLogRepository extends JpaRepository<CashLog, Long> {
	Page<CashLog> findAllByUserId(Long userId, Pageable pageable);

	@Query("SELECT cl FROM CashLog cl " +
			"JOIN FETCH cl.user u " +
			"JOIN FETCH cl.coupon c " +
			"WHERE u.id = :userId AND cl.coupon IS NOT NULL")
	Slice<CashLog> findAllByUserIdWithCoupon(@Param("userId") Long userId, Pageable pageable);
}
