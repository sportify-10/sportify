package com.sparta.sportify.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

	USER_NOT_FOUND(404, "존재하지 않는 유저입니다"),
	CASH_LOG_NOT_FOUND(404, "캐시 사용 내역이 없습니다."),
	MATCH_NOT_FOUND(404, "경기를 찾을 수 없습니다."),
	RESERVATION_NOT_FOUND_FOR_TEAM(404, "팀에 대한 예약을 찾을 수 없습니다."),
	MATCHRESULT_NOT_FOUND(404, "경기 결과를 찾을 수 없습니다."),
	STADIUMTIME_NOT_FOUND(404, "경기 시간을 찾을 수 없습니다"),
	;

	private final int status;
	private final String message;

	ErrorCode(int status, String message) {
		this.status = status;
		this.message = message;
	}

}
