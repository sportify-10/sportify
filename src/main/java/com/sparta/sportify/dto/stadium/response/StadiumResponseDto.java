package com.sparta.sportify.dto.stadium.response;

import com.sparta.sportify.entity.Stadium;

import lombok.Getter;

@Getter
public class StadiumResponseDto {
	private Long id;

	public StadiumResponseDto(Stadium stadium) {
		this.id = stadium.getId();
	}
}
