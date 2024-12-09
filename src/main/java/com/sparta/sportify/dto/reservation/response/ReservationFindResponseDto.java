package com.sparta.sportify.dto.reservation.response;


import com.sparta.sportify.dto.stadium.response.StadiumResponseDto;
import com.sparta.sportify.entity.Reservation;
import com.sparta.sportify.entity.Stadium;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReservationFindResponseDto {
    Long userId;
    Long reservationId;
    StadiumResponseDto stadium;
    LocalDate reservationDate;
    Integer totalAmount;

    public ReservationFindResponseDto(Reservation reservation){
        this.userId = reservation.getUser().getId();
        this.reservationId = reservation.getId();
        this.stadium = new StadiumResponseDto(reservation.getMatch().getStadiumTime().getStadium());
        this.reservationDate = reservation.getReservationDate();
        this.totalAmount = reservation.getTotalAmount();
    }

}
