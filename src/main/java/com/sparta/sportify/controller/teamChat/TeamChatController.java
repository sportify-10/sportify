package com.sparta.sportify.controller.teamChat;

import com.sparta.sportify.dto.teamChat.response.TeamChatResponseDto;
import com.sparta.sportify.security.UserDetailsImpl;
import com.sparta.sportify.service.TeamChatService;
import com.sparta.sportify.util.api.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class TeamChatController {
    private final TeamChatService teamChatService;

    @PostMapping("/join/{teamId}")
    public ResponseEntity<ApiResult<String>> joinTeamChatting(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        teamChatService.joinTeamChatting(teamId, userDetails);
        String webSocketUrl = "ws://localhost:8080/ws/" + teamId;
        return ResponseEntity.ok(
                ApiResult.success(
                        "팀 채팅 참가 성공",
                        webSocketUrl)
        );
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResult<List<TeamChatResponseDto>>> getChatData(
            @PathVariable Long teamId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        return ResponseEntity.ok(
                ApiResult.success(
                        "팀 채팅 내역 조회 성공",
                        teamChatService.getChatData(teamId, userDetails)
                )
        );
    }
}