package com.sparta.sportify.dto.cash.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CashRefundRequestDto {
    @NotBlank
    private Long cashLogId;
    @NotBlank
    private Long amount;
}
