package com.sparta.sportify.dto.cash.response;

import java.time.LocalDateTime;

import com.sparta.sportify.entity.cashLog.CashType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CashLogsResponseDto {
	 private Long amount;
	 private LocalDateTime createAt;
	 private CashType type;
}
