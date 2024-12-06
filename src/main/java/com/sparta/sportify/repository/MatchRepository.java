package com.sparta.sportify.repository;

import com.sparta.sportify.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByIdAndDateAndTime(Long id, LocalDate date, Integer time);
}
