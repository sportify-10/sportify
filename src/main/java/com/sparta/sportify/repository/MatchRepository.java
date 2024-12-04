package com.sparta.sportify.repository;

import com.sparta.sportify.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {
}
