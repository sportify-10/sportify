package com.sparta.sportify.dto.stadium.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StadiumUpdateRequestDto {
	private String stadiumName;
	private String location;
	private int aTeamCount;
	private int bTeamCount;
	private String description;
	private Long price;
}
