package com.sparta.sportify.exception;

import lombok.Getter;

import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

	USER_NOT_FOUND(404, "존재하지 않는 유저입니다"),
	CASH_LOG_NOT_FOUND(404, "캐시 사용 내역이 없습니다.");

	private final int status;
	private final String message;

	ErrorCode(int status, String message) {
		this.status = status;
		this.message = message;
	}

}
