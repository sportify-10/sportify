package com.sparta.sportify.dto.cash.response;

import java.time.LocalDateTime;

import com.sparta.sportify.entity.CashLog;
import com.sparta.sportify.entity.CashType;

import lombok.Getter;

@Getter
public class CashResponseDto {
	private Long userId;
	private Long balance;
	private LocalDateTime createAt;
	private CashType type;
	private Long ChargeAmount;

	public CashResponseDto(CashLog cashLog) {
		this.userId = cashLog.getUser().getId();
		this.type = cashLog.getType();
		this.ChargeAmount = cashLog.getPrice();
		this.balance = cashLog.getUser().getCash();
		this.createAt = cashLog.getCreateAt();
	}
}
