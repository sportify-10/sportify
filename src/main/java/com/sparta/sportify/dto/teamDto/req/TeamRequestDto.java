package com.sparta.sportify.dto.teamDto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TeamRequestDto {
    @NotBlank
    private String teamName;
    @NotBlank
    private String region;
    @NotBlank
    private String activityTime;
    @NotBlank
    private String skillLevel;
    @NotBlank
    private String sportType;
    @NotBlank
    private String description;
}
