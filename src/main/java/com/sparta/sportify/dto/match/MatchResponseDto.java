package com.sparta.sportify.dto.match;

import java.time.LocalDate;

public class MatchResponseDto {
	private Long id;
	private LocalDate date;
	private String time;
	private Integer aTeamCount;
	private Integer bTeamCount;

	public MatchResponseDto(Long id, LocalDate date, String time, Integer aTeamCount, Integer bTeamCount) {
		this.id = id;
		this.date = date;
		this.time = time;
		this.aTeamCount = aTeamCount;
		this.bTeamCount = bTeamCount;
	}
}
