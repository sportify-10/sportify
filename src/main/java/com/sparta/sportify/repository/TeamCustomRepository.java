package com.sparta.sportify.repository;

import com.sparta.sportify.entity.team.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TeamCustomRepository {
    Page<Team> findAllWithFilters(String sportType, String skillLevel, String region, Pageable pageable);
}
