package com.sparta.sportify.dto.stadiumTime.response;

import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.entity.StadiumTime;

import lombok.Getter;

@Getter
public class StadiumTimeResponseDto {
	private String cronExpression;
	private Long stadiumId;

	public StadiumTimeResponseDto(StadiumTime stadiumTime, Stadium stadium) {
		this.cronExpression = stadiumTime.getCron();
		this.stadiumId = stadium.getId();
	}
}
