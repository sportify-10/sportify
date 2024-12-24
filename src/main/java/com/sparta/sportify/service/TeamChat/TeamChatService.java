package com.sparta.sportify.service.TeamChat;

import java.util.List;
import java.util.stream.Collectors;

import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.teamChat.response.TeamChatResponseDto;
import com.sparta.sportify.entity.teamChat.TeamChat;
import com.sparta.sportify.repository.TeamChat.TeamChatRepository;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamChatService {
	private final TeamChatRepository teamChatRepository;
	private final TeamMemberRepository teamMemberRepository;
	private final RedissonClient redissonClient;

	public void joinTeamChatting(Long teamId, UserDetailsImpl userDetails) {
		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId)
			.orElseThrow(()-> new IllegalArgumentException("해당 팀이 아닙니다"));
	}

	public List<TeamChatResponseDto> getChatData(Long teamId, UserDetailsImpl userDetails) {
		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId)
			.orElseThrow(() -> new IllegalArgumentException("해당 팀이 아닙니다"));

		RList<TeamChatResponseDto> messageList = redissonClient.getList("team:" + teamId + ":messages");

		if (!messageList.isEmpty()) {
			return messageList.stream()
				.collect(Collectors.toList());
		}

		List<TeamChat> teamChats = teamChatRepository.findByTeamId(teamId);

		List<TeamChatResponseDto> chatDataList = teamChats.stream()
			.map(teamChat -> new TeamChatResponseDto(
				teamChat.getUser().getId(),
				teamChat.getTeam().getId(),
				teamChat.getContent(),
				teamChat.getCreateAt()))
			.collect(Collectors.toList());

		messageList.addAll(chatDataList);

		return chatDataList;
	}
}
