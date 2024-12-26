package com.sparta.sportify.dto.teamDto;

import com.sparta.sportify.entity.teamMember.TeamMember;
import lombok.Getter;

@Getter
public class TeamMemberResponseDto {
    private Long teamMemberId;
    private Long teamId;
    private TeamMember.Status status;


    public TeamMemberResponseDto(TeamMember teamMember) {
        this.teamMemberId = teamMember.getTeamMemberId();
        this.teamId = teamMember.getTeam().getId();
        this.status = teamMember.getStatus();
    }
}
