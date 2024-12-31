package com.sparta.sportify.dto.teamDto.req;

import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoleRequestDto {
    @NotBlank
    private Long userId;
    @NotBlank
    private TeamMemberRole role;
}
