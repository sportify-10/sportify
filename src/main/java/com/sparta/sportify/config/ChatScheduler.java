package com.sparta.sportify.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.redisson.api.RKeys;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sparta.sportify.dto.teamChat.response.TeamChatResponseDto;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.teamChat.TeamChat;
import com.sparta.sportify.repository.TeamChatRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class ChatScheduler {
	private final UserRepository userRepository;
	private final RedissonClient redissonClient;
	private final TeamRepository teamRepository;
	private final TeamChatRepository teamChatRepository;

	@Scheduled(fixedRate = 5000) //5초
	public void saveChatsToDB() {
		RKeys keys = redissonClient.getKeys();
		Iterable<String> messageKeys = keys.getKeysByPattern("teamChats::team:*:messages");

		List<TeamChatResponseDto> messages = new ArrayList<>();
		for (String key : messageKeys) {
			RList<TeamChatResponseDto> messageList = redissonClient.getList(key);
			messages.addAll(messageList);
			messageList.clear();
		}

		if (!messages.isEmpty()) {
			saveMessagesToDB(messages);
		}
	}

	private void saveMessagesToDB(List<TeamChatResponseDto> messages) {
		List<TeamChat> teamChats = messages.stream().map(chatData -> {
			try {
				Long teamId = chatData.getTeamId();
				Long userId = chatData.getUserId();

				Team team = teamRepository.findById(teamId)
					.orElseThrow(() -> new IllegalArgumentException("해당 팀이 존재하지 않습니다."));
				User user = userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다"));

				return TeamChat.builder()
					.content(chatData.getContent())
					.user(user)
					.team(team)
					.createAt(LocalDateTime.parse(chatData.getTimestamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
					.build();
			} catch (Exception e) {
				throw new RuntimeException("메시지 변환 실패: " + chatData, e);
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());

		if (!teamChats.isEmpty()) {
			teamChatRepository.saveAll(teamChats);
		}
	}
}
