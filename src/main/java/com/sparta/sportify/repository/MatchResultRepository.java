package com.sparta.sportify.repository;

import java.util.Optional;

import com.sparta.sportify.entity.matchResult.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
	Optional<MatchResult> findByMatchId(Long matchId);
}
