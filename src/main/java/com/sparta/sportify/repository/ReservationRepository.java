package com.sparta.sportify.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.reservation.Reservation;
import com.sparta.sportify.entity.user.User;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@Query("SELECT COUNT(r) > 0 FROM Reservation r JOIN r.match m WHERE r.user = :user AND m.time = :time AND r.reservationDate = :reservationDate")
	boolean existsByUserAndMatchTimeAndReservationDate(@Param("user") User user, @Param("time") Integer time,
		@Param("reservationDate") LocalDate reservationDate);

	@Query("SELECT COUNT(r) > 0 FROM Reservation r JOIN r.match m WHERE r.user IN :users AND m.time = :time AND r.reservationDate = :reservationDate")
	boolean existsByUsersAndMatchTimeAndReservationDate(
		@Param("users") List<User> users,
		@Param("time") Integer time,
		@Param("reservationDate") LocalDate reservationDate
	);

	Slice<Reservation> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

	@Query("SELECT r FROM Reservation r " +
		"JOIN FETCH r.user " +
		"LEFT JOIN FETCH r.team " +
		"JOIN FETCH r.match " +
		"WHERE r.match = :match")
	List<Reservation> findAllByMatch(Match match);
}
