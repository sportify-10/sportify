package com.sparta.sportify.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.sportify.entity.teamArticle.TeamArticle;

public interface TeamArticleRepository extends JpaRepository<TeamArticle, Long> {

	Page<TeamArticle> findAllByTeamIdAndDeletedAtIsNull(Long teamId, Pageable pageable);
}
