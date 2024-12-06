package com.sparta.sportify.dto.teamDto;

import lombok.Getter;

@Getter
public class TeamRequestDto {
    private String teamName;
    private String region;
    private String activityTime;
    private String skillLevel;
    private String sportType;
    private String description;
}
