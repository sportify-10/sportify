package com.sparta.sportify.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashResponseDto;
import com.sparta.sportify.entity.CashLog;
import com.sparta.sportify.entity.CashType;
import com.sparta.sportify.repository.CashLogRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CashService {

	private final CashLogRepository cashLogRepository;
	private final UserRepository userRepository;

	public CashResponseDto addCash(UserDetailsImpl userDetails, CashRequestDto cashRequestDto) {
		CashLog cashLog = cashLogRepository.save(
			CashLog.builder()
				.price(cashRequestDto.getAmount())
				.createAt(LocalDateTime.now())
				.type(CashType.CHARGE)
				.user(userDetails.getUser())
				.build());

		//유저 cash 업데이트
		userDetails.getUser().addCash(cashRequestDto);
		userRepository.save(userDetails.getUser());

		return new CashResponseDto(cashLog);
	}
}
