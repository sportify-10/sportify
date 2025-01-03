package com.sparta.sportify.dto.user.res;

import lombok.*;

@Getter
@AllArgsConstructor
public class UserTeamResponseDto {
	private Long id;
	private String teamName;
	private String region;
	private String activityTime;
	private String skillLevel;
	private String sprotType;
	private int teamPoints;
	private double winRate;
}
