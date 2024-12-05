package com.sparta.sportify.dto.stadium.response;

import java.time.LocalDateTime;

import com.sparta.sportify.entity.Stadium;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StadiumResponseDto {
	private Long id;
	private String stadiumName;
	private String location;
	private int aTeamCount;
	private int bTeamCount;
	private String description;
	private int price;
	private String status;
	private LocalDateTime deletedAt;

	public StadiumResponseDto(Stadium stadium) {
		this.id = stadium.getId();
		this.stadiumName = getStadiumName();
		this.location = getLocation();
		this.aTeamCount = getATeamCount();
		this.bTeamCount = getBTeamCount();
		this.description = getDescription();
		this.price = getPrice();
		this.status = stadium.getStatus();
		this.deletedAt = stadium.getDeletedAt();
	}
}
