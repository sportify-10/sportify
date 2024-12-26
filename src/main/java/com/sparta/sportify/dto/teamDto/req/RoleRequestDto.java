package com.sparta.sportify.dto.teamDto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoleRequestDto {
    @NotBlank
    private Long userId;
    @NotBlank
    private String role;
}
