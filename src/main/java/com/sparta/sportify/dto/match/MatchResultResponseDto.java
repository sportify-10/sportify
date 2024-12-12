package com.sparta.sportify.dto.match;

import java.time.LocalDate;

import com.sparta.sportify.entity.MatchStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchResultResponseDto {
	private Long id;
	private Integer teamAScore;
	private Integer teamBScore;
	private MatchStatus matchStatus;
	private LocalDate matchDate;
}