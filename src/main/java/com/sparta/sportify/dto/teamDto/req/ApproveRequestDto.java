package com.sparta.sportify.dto.teamDto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApproveRequestDto {
    @NotBlank
    private Long userId;
    @NotBlank
    private boolean approve;
}
