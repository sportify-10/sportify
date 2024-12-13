package com.sparta.sportify.dto.teamDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoleRequestDto {
    private Long userId;
    private String role;
}
