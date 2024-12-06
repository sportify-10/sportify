package com.sparta.sportify.repository;

import com.sparta.sportify.entity.Reservation;
import com.sparta.sportify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) > 0 FROM Reservation r JOIN r.match m WHERE r.user = :user AND m.time = :time AND r.reservationDate = :reservationDate")
    boolean existsByUserAndMatchTimeAndReservationDate(@Param("user") User user, @Param("time") Integer time, @Param("reservationDate") LocalDate reservationDate);
}
