package com.sparta.sportify.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamChat.TeamChat;
import com.sparta.sportify.repository.TeamChat.TeamChatRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TeamChatWebSocketHandler extends TextWebSocketHandler {

    private final TeamChatRepository teamChatRepository;

    // 연결된 WebSocket 세션 관리
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final TeamRepository teamRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        sessions.add(session);
        System.out.println("새로운 WebSocket 연결: " + session.getId());

        UserDetailsImpl userDetails = (UserDetailsImpl) session.getAttributes().get("user");

        // 입장 메시지
        String joinMessage = userDetails.getUser().getName() + "이(가) 입장했습니다.";
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(joinMessage));
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("수신된 메시지: " + message.getPayload());
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage("팀 채팅 메시지: " + message.getPayload()));
            }
        }

        //유저정보
        UserDetailsImpl userDetails = (UserDetailsImpl) session.getAttributes().get("user");

        //팀 정보
        Long teamId = (Long) session.getAttributes().get("teamId");
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀이 존재하지 않습니다."));

        //채팅 내역 DB 저장
        TeamChat teamChat = TeamChat.builder()
                .content(message.getPayload())
                .user(userDetails.getUser())
                .team(team)
                .createAt(LocalDateTime.now())
                .build();

        teamChatRepository.save(teamChat);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws IOException {
        sessions.remove(session);
        System.out.println("WebSocket 연결 종료: " + session.getId());

        UserDetailsImpl userDetails = (UserDetailsImpl) session.getAttributes().get("user");

        // 퇴장 메시지
        String leaveMessage = userDetails.getUser().getName() + "이(가) 퇴장했습니다.";
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(leaveMessage));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.out.println("WebSocket 오류 발생: " + exception.getMessage());
    }
}
