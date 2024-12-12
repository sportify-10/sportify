package com.sparta.sportify.dto.stadium.response;

import java.time.LocalDateTime;

import com.sparta.sportify.entity.Stadium;
import com.sparta.sportify.entity.StadiumStatus;

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
