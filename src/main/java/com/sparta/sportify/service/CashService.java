package com.sparta.sportify.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.cash.request.CashRequestDto;
import com.sparta.sportify.dto.cash.response.CashLogsResponseDto;
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

	public List<CashLogsResponseDto> getCashLogs(UserDetailsImpl userDetails, int page, int size) {
		Pageable pageable = PageRequest.of(page-1, size);
		Page<CashLog> cashLogs = cashLogRepository.findAllByUserId(userDetails.getUser().getId(), pageable);

		if(cashLogs.isEmpty()) {
			throw new IllegalArgumentException("캐시 사용 내역이 없습니다");
		}

		return cashLogs.stream().map(cashLog -> new CashLogsResponseDto(
			cashLog.getPrice(),
			cashLog.getCreateAt(),
			cashLog.getType()
		)).collect(Collectors.toList());
	}
}
