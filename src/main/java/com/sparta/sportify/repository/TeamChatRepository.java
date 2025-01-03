package com.sparta.sportify.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sparta.sportify.entity.teamChat.TeamChat;

public interface TeamChatRepository extends JpaRepository<TeamChat, Long> {
	List<TeamChat> findByTeamId(Long teamId);
}
