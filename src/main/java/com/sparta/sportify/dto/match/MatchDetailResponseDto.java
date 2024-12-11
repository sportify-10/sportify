package com.sparta.sportify.dto.match;

import java.time.LocalDate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MatchDetailResponseDto {
	private final Long matchId;
	private final LocalDate date;
	private final Integer time;
	private final Integer aTeamCount;
	private final Integer bTeamCount;
	private final String stadiumName;
	private final String status;
}
