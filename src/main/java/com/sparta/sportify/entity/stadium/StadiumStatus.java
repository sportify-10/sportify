package com.sparta.sportify.entity.stadium;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StadiumStatus {
	PENDING("대기"),APPROVED("승인"),REJECTED("거절");

	private final String value;
}
