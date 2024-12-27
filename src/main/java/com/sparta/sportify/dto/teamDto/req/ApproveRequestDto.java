package com.sparta.sportify.dto.teamDto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveRequestDto {
    @NotBlank
    private Long userId;
    @NotBlank
    private boolean approve;

    public ApproveRequestDto(Long id) {
        this.userId = id;
    }
}
