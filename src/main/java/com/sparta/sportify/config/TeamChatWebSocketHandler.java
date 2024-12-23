package com.sparta.sportify.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.sparta.sportify.entity.Team;
import com.sparta.sportify.repository.TeamChat.TeamChatRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.util.BadWordFilter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamChatWebSocketHandler extends TextWebSocketHandler {

	private final TeamChatRepository teamChatRepository;
	private final BadWordFilter badWordFilter;
	private final RedissonClient redissonClient;

	// 연결된 WebSocket 세션 관리
	private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
	private final TeamRepository teamRepository;
	private final Map<Long, List<WebSocketSession>> teamSessions = new HashMap<>();  // 팀별 세션 관리

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws IOException {

		Long teamId = (Long)session.getAttributes().get("teamId");
		teamSessions.putIfAbsent(teamId, new ArrayList<>());
		teamSessions.get(teamId).add(session);

		UserDetailsImpl userDetails = (UserDetailsImpl) session.getAttributes().get("user");

		// 입장 메시지
		String joinMessage = userDetails.getUser().getName() + "이(가) 입장했습니다.";
		sendMessageToTeamSessions(teamId, joinMessage, session);
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		System.out.println("수신된 메시지: " + message.getPayload());

		//유저정보
		UserDetailsImpl userDetails = (UserDetailsImpl)session.getAttributes().get("user");

		//팀 정보
		Long teamId = (Long)session.getAttributes().get("teamId");
		Team team = teamRepository.findById(teamId)
			.orElseThrow(() -> new IllegalArgumentException("해당 팀이 존재하지 않습니다."));

		String containsProfanity =  badWordFilter.containsSimilarBadWord(message.getPayload());

		sendMessageToTeamSessions(teamId, containsProfanity, session);

		// //채팅 내역 DB 저장
		// TeamChat teamChat = TeamChat.builder()
		// 	.content(containsProfanity)
		// 	.user(userDetails.getUser())
		// 	.team(team)
		// 	.createAt(LocalDateTime.now())
		// 	.build();
		//
		// teamChatRepository.save(teamChat);

		RList<Map<String, String>> messageList = redissonClient.getList("team:" + teamId + ":messages");

		Map<String, String> chatData = new HashMap<>();
		chatData.put("content", containsProfanity);
		chatData.put("userId", userDetails.getUser().getId().toString());
		chatData.put("teamId", teamId.toString());
		chatData.put("timestamp", LocalDateTime.now().toString());

		messageList.add(chatData); // Redis 리스트에 추가
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {

		Long teamId = (Long)session.getAttributes().get("teamId");
		teamSessions.putIfAbsent(teamId, new ArrayList<>());
		teamSessions.get(teamId).remove(session);

		System.out.println("WebSocket 연결 종료: " + session.getId());

		UserDetailsImpl userDetails = (UserDetailsImpl) session.getAttributes().get("user");

		// 퇴장 메시지
		String leaveMessage = userDetails.getUser().getName() + "이(가) 퇴장했습니다.";
		sendMessageToTeamSessions(teamId, leaveMessage, session);
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) {
		System.out.println("WebSocket 오류 발생: " + exception.getMessage());
	}

	private void sendMessageToTeamSessions(Long teamId, String message, WebSocketSession session) throws IOException {
		List<WebSocketSession> teamSessionList = teamSessions.get(teamId);

		if (teamSessionList != null) {
			for (int i = 0; i < teamSessionList.size(); i++) {
				WebSocketSession s = teamSessionList.get(i);
				// 본인에게는 메시지를 보내지 않도록 조건 추가
				if (s.isOpen() && !s.getId().equals(session.getId())) {
					s.sendMessage(new TextMessage(message));
				}
			}
		}
	}
}
