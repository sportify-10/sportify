package com.sparta.sportify.service;

import java.util.List;
import java.util.stream.Collectors;

import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.sparta.sportify.dto.teamChat.response.TeamChatResponseDto;
import com.sparta.sportify.entity.teamChat.TeamChat;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
import com.sparta.sportify.repository.TeamChatRepository;
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
			.orElseThrow(() -> new CustomApiException(ErrorCode.NOT_A_MEMBER_OF_THE_TEAM));
	}

	public List<TeamChatResponseDto> getChatData(Long teamId, UserDetailsImpl userDetails) {
		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId)
			.orElseThrow(() -> new CustomApiException(ErrorCode.NOT_A_MEMBER_OF_THE_TEAM));

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
