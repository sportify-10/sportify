package com.sparta.sportify.dto.match;

import java.time.LocalDate;

import com.sparta.sportify.entity.matchResult.MatchStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultResponseDto {
	private Long id;
	private Integer teamAScore;
	private Integer teamBScore;
	private MatchStatus matchStatus;
	private LocalDate matchDate;
}