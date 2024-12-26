package com.sparta.sportify.repository;

import com.sparta.sportify.entity.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long>, TeamCustomRepository {
}
