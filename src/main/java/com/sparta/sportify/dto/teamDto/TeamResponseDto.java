package com.sparta.sportify.dto.teamDto;

import com.sparta.sportify.entity.Team;
import lombok.Getter;

@Getter
public class TeamResponseDto {
    private Long id;
    private String teamName;
    private String region;
    private String activityTime;
    private String skillLevel;
    private String sportType;
    private String description;

    public TeamResponseDto(Team team) {
        this.id = team.getId();
        this.teamName = team.getTeamName();
        this.region = team.getRegion();
        this.activityTime = team.getActivityTime();
        this.skillLevel = team.getSkillLevel();
        this.sportType = team.getSportType();
        this.description = team.getDescription();
    }
}
