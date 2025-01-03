package com.sparta.sportify.repository;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.sportify.entity.match.Match;
import com.sparta.sportify.entity.reservation.ReservationStatus;

import org.springframework.data.jpa.repository.Query;

public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByIdAndDateAndTime(Long id, LocalDate date, Integer time);
    Optional<Match> findByStadiumTimeIdAndDateAndTime(Long id, LocalDate date, Integer time);


    // 특정 시간 범위에 시작하는 경기 찾기 (time과 date를 기반으로 startTime 계산)
    @Query("SELECT m FROM Match m WHERE " +
            "FUNCTION('TIMESTAMP', m.date, FUNCTION('SEC_TO_TIME', m.time * 3600)) BETWEEN :start AND :end " +
            "OR FUNCTION('TIMESTAMP', m.date, FUNCTION('SEC_TO_TIME', (m.time + 2) * 3600)) BETWEEN :start AND :end")
    List<Match> findMatchesByStartTimeBetween(@Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);




	@Query("SELECT m, SUM(r.totalAmount) " +
			"FROM Match m " +
			"LEFT JOIN FETCH m.stadiumTime st " +
			"LEFT JOIN FETCH st.stadium s " +
			"LEFT JOIN Reservation r ON r.match.id = m.id AND r.status = :status " +
			"WHERE s.id = :stadiumId " +
			"GROUP BY m.id")
	Page<Object[]> findMatchesWithTotalAmountByStadiumId(Long stadiumId, ReservationStatus status, Pageable pageable);
}

