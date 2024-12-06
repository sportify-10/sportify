package com.sparta.sportify.dto.reservation.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReservationResponseDto {
    List<Long> reservationId;

    public ReservationResponseDto(Long id){
        reservationId = new ArrayList<>();
        reservationId.add(id);
    }
}
