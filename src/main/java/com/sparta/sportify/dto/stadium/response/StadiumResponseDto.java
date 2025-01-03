package com.sparta.sportify.dto.stadium.response;

import java.time.LocalDateTime;

import com.sparta.sportify.entity.stadium.Stadium;
import com.sparta.sportify.entity.stadium.StadiumStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StadiumResponseDto {
	private Long id;
	private String stadiumName;
	private String location;
	private int aTeamCount;
	private int bTeamCount;
	private String description;
	private Long price;
	private StadiumStatus status;
	private LocalDateTime deletedAt;

	public StadiumResponseDto(Stadium stadium) {
		this.id = stadium.getId();
		this.stadiumName = stadium.getStadiumName();
		this.location = stadium.getLocation();
		this.aTeamCount = stadium.getATeamCount();
		this.bTeamCount = stadium.getBTeamCount();
		this.description = stadium.getDescription();
		this.price = stadium.getPrice();
		this.status = stadium.getStatus();
		this.deletedAt = stadium.getDeletedAt();
	}
}
