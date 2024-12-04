package com.sparta.sportify.repository;

import com.sparta.sportify.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
}
