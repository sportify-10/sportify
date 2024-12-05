package com.sparta.sportify.dto.stadiumTime.request;

import java.util.List;

import lombok.Getter;

@Getter
public class StadiumTimeCreateRequestDto {
	private List<String> hours;
	private List<String> weeks;
}
