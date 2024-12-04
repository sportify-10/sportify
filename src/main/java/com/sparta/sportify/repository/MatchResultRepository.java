package com.sparta.sportify.repository;

import com.sparta.sportify.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {
}
