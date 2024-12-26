package com.sparta.sportify.entity.team;

import com.sparta.sportify.dto.teamDto.TeamRequestDto;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "teams")
public class Team {
    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String teamName;
    private String region;
    private String activityTime;
    private String skillLevel;
    private String sportType;
    private String description;
    private Integer teamPoints;
    private Float winRate;
    private LocalDateTime deletedAt;

    public Team(TeamRequestDto requestDto) {
        this.teamName = requestDto.getTeamName();
        this.region = requestDto.getRegion();
        this.activityTime = requestDto.getActivityTime();
        this.skillLevel = requestDto.getSkillLevel();
        this.sportType = requestDto.getSportType();
        this.description = requestDto.getDescription();
    }

    public void updateData(String teamName, String region, String activityTime, String skillLevel, String sportType, String description) {
        this.setTeamName(teamName);
        this.setRegion(region);
        this.setActivityTime(activityTime);
        this.setSkillLevel(skillLevel);
        this.setSportType(sportType);
        this.setDescription(description);
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    // Getters and Setters
}
