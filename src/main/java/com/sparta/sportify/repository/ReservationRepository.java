package com.sparta.sportify.repository;

import com.sparta.sportify.entity.Reservation;
import com.sparta.sportify.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) > 0 FROM Reservation r JOIN r.match m WHERE r.user = :user AND m.time = :time AND r.reservationDate = :reservationDate")
    boolean existsByUserAndMatchTimeAndReservationDate(@Param("user") User user, @Param("time") Integer time, @Param("reservationDate") LocalDate reservationDate);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r JOIN r.match m WHERE r.user IN :users AND m.time = :time AND r.reservationDate = :reservationDate")
    boolean existsByUsersAndMatchTimeAndReservationDate(
            @Param("users") List<User> users,
            @Param("time") Integer time,
            @Param("reservationDate") LocalDate reservationDate
    );

    Slice<Reservation> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    @Query("SELECT SUM(r.totalAmount) FROM Reservation r WHERE r.match.id = :matchId")
    Integer findTotalAmountByMatchId(@Param("matchId") Long matchId);
}
