package com.sparta.sportify.dto.cash.response;

import com.sparta.sportify.entity.CashLog;
import com.sparta.sportify.entity.CashType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CashResponseDto {
	private Long userId;
	private Long balance;
	private LocalDateTime createAt;
	private CashType type;
	private String tid;
	private Long ChargeAmount;

	public CashResponseDto(CashLog cashLog) {
		this.userId = cashLog.getUser().getId();
		this.type = cashLog.getType();
		this.ChargeAmount = cashLog.getPrice();
		this.balance = cashLog.getUser().getCash();
		this.createAt = cashLog.getCreateAt();
	}

}
