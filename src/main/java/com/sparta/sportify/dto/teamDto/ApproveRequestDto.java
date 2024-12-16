package com.sparta.sportify.dto.teamDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveRequestDto {
    private Long userId;
    private boolean approve;

    public ApproveRequestDto(Long id) {
        this.userId = id;
    }
}
