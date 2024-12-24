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

	//캐시 적용x
	// public List<TeamChatResponseDto> getChatData(Long teamId, UserDetailsImpl userDetails) {
	// 	teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId)
	// 		.orElseThrow(()-> new IllegalArgumentException("해당 팀이 아닙니다"));
	//
	// 	List<TeamChat> teamChats = teamChatRepository.findByTeamId(teamId);
	//
	// 	return teamChats.stream().map(teamChat -> new TeamChatResponseDto(
	// 			teamChat.getUser().getId(), teamChat.getTeam().getId(), teamChat.getContent()))
	// 		.collect(Collectors.toList());
	// }

	//캐시에 있는 데이터만 조회됨
	//키 값을 다르게, 저장과 조회 역할의 키 -> 조회 시 두 키 합쳐서 조회
	//최근 몇개만 보여주기
	//@Cacheable(value = "teamChats", key = "'team:' + #teamId + ':messages'")
	public List<TeamChatResponseDto> getChatData(Long teamId, UserDetailsImpl userDetails) {
		teamMemberRepository.findByUserIdAndTeamId(userDetails.getUser().getId(), teamId)
			.orElseThrow(() -> new IllegalArgumentException("해당 팀이 아닙니다"));

		RList<TeamChatResponseDto> messageList = redissonClient.getList("team:" + teamId + ":messages");

		if (!messageList.isEmpty()) {
			return messageList.stream()
				.collect(Collectors.toList());
		}
		//캐시에 있는 데이터만 조회됨
		//키 값을 다르게, 저장과 조회 역할의 키 -> 조회 시 두 키 합쳐서 조회
		//최근 몇개만 보여주기

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
