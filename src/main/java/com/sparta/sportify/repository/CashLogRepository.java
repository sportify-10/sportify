package com.sparta.sportify.repository;

import java.util.List;

import com.sparta.sportify.entity.CashLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashLogRepository extends JpaRepository<CashLog, Long> {
	Page<CashLog> findAllByUserId(Long userId, Pageable pageable);
}
