package com.sparta.sportify.repository;

import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
	Optional<Match> findByIdAndDateAndTime(Long id, LocalDate date, Integer time);

	Optional<Match> findByStadiumTimeIdAndDateAndTime(Long id, LocalDate date, Integer time);

	@Query("SELECT m, SUM(r.totalAmount) " +
		"FROM Match m " +
		"LEFT JOIN FETCH m.stadiumTime st " +
		"LEFT JOIN FETCH st.stadium s " +
		"LEFT JOIN Reservation r ON r.match.id = m.id AND r.status = :status " +
		"WHERE s.id = :stadiumId " +
		"GROUP BY m.id")
	Page<Object[]> findMatchesWithTotalAmountByStadiumId(Long stadiumId, ReservationStatus status, Pageable pageable);
}

