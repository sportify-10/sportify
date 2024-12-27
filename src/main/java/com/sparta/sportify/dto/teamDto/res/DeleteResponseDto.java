package com.sparta.sportify.dto.teamDto.res;

import com.sparta.sportify.entity.team.Team;
import lombok.Getter;

@Getter
public class DeleteResponseDto {
    private Long id;

    public DeleteResponseDto(Team team) {
        this.id = team.getId();
    }
}
