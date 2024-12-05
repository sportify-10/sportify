package com.sparta.sportify.entity;

import com.sparta.sportify.dto.teamDto.TeamRequestDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
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

    @OneToMany(mappedBy = "teams", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMember> teamMembers;
//
//    @OneToMany(mappedBy = "teams", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Reservation> reservations;

    public Team(TeamRequestDto requestDto) {
        this.teamName = requestDto.getTeamName();
        this.region = requestDto.getRegion();
        this.activityTime = requestDto.getActivityTime();
        this.skillLevel = requestDto.getSkillLevel();
        this.sportType = requestDto.getSportType();
        this.description = requestDto.getDescription();
    }

    public void updateData(TeamRequestDto requestDto){
        this.teamName = requestDto.getTeamName();
        this.region = requestDto.getRegion();
        this.activityTime = requestDto.getActivityTime();
        this.skillLevel = requestDto.getSkillLevel();
        this.sportType = requestDto.getSportType();
        this.description = requestDto.getDescription();
    }

    public void softDelete() {
        this.setDeletedAt(LocalDateTime.now());
    }
    // Getters and Setters
}
