package com.sparta.sportify.dto.match.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MatchByStadiumResponseDto {
	private final Long stadiumId;
	private final String stadiumName;
	private final String stadiumDescription;
	private final String stadiumLocation;
	private final String startTime;
	private final String endTime;
	private final String status;
}
