package com.sparta.sportify.dto.match.response;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MatchesByDateResponseDto {
	// private final List<Long> stadiumIds;
	// private final List<String> stadiumNames;
	// private final List<String> stadiumDescriptions;
	// private final List<String> stadiumLocations;
	// private final List<String> startTimes;
	// private final List<String> endTimes;

	private final List<MatchByStadiumResponseDto> data;
}
