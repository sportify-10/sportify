package com.sparta.sportify.dto.teamDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApproveResponseDto {
    private Long userId;
    private boolean approve;
}
