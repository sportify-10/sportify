package com.sparta.sportify.repository;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.sportify.entity.match.Match;
import org.springframework.data.jpa.repository.Query;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByIdAndDateAndTime(Long id, LocalDate date, Integer time);
    Optional<Match> findByStadiumTimeIdAndDateAndTime(Long id, LocalDate date, Integer time);
    Page<Match> findByStadiumTimeStadiumId(Long stadiumId, Pageable pageable);


    //List<Match> findMatchesByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    @Query("SELECT m FROM Match m WHERE m.startTime BETWEEN :start AND :end")
    List<Match> findMatchesByStartTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT m FROM Match m WHERE m.startingAt = :startingAt")
    List<Match> findMatchesStartingAt(@Param("startingAt") LocalDateTime startingAt);


}
