package com.sparta.sportify.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.sportify.entity.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByIdAndDateAndTime(Long id, LocalDate date, Integer time);
    Optional<Match> findByStadiumTimeIdAndDateAndTime(Long id, LocalDate date, Integer time);
    Page<Match> findAllByStadiumTimeIdAndDateAndTime(Long id, LocalDate date, Integer time, Pageable pageable);
    Page<Match> findByStadiumTime_Stadium_Id(Long stadiumId, Pageable pageable);
}
