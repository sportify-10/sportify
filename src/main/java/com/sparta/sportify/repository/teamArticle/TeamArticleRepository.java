package com.sparta.sportify.repository.teamArticle;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.sportify.entity.teamArticle.TeamArticle;

public interface TeamArticleRepository extends JpaRepository<TeamArticle, Long> {

}
