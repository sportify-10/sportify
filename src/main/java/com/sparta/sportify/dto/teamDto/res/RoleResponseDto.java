package com.sparta.sportify.dto.teamDto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoleResponseDto {
    private Long userId;
    private String role;
}
