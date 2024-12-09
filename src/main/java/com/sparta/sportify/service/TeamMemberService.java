package com.sparta.sportify.service;

import com.sparta.sportify.dto.teamDto.TeamMemberResponseDto;
import com.sparta.sportify.entity.Team;
import com.sparta.sportify.entity.TeamMember;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamMemberResponseDto applyToTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다"));
        // 승인된 상태(가입) 확인
        boolean isAlreadyApproved = teamMemberRepository.existsByUserAndTeamAndStatus(user, team, TeamMember.Status.APPROVED);
        if (isAlreadyApproved) {
            throw new IllegalStateException("이미 해당 팀에 가입되어 있습니다");
        }

        TeamMember teamMember = new TeamMember(user, team);
        teamMemberRepository.save(teamMember);
        return new TeamMemberResponseDto(teamMember);
    }
}
