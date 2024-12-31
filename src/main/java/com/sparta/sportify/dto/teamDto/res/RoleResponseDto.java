package com.sparta.sportify.dto.teamDto.res;

import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleResponseDto {
    private Long userId;
    private TeamMemberRole role;
}
