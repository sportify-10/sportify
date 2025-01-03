package com.sparta.sportify.dto.stadium.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StadiumMatchResponseDto {
	private Long stadiumId;
	private String stadiumName;
	private LocalDate matchDate;
	private String matchTime;
	private Integer totalAmount;
	private Integer teamACount;
	private Integer teamBCount;
}
