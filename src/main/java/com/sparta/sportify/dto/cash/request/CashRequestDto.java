package com.sparta.sportify.dto.cash.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CashRequestDto {
    @NotBlank
    private Long amount;
}
