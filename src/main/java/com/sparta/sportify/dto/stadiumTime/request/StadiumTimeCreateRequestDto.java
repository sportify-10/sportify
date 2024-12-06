package com.sparta.sportify.dto.stadiumTime.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StadiumTimeCreateRequestDto {
	private List<String> hours;
	private List<String> weeks;
}
