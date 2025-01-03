package com.sparta.sportify.dto.teamDto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApproveResponseDto {
    private Long userId;
    private boolean approve;
}
