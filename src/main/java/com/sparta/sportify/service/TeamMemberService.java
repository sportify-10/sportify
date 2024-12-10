package com.sparta.sportify.service;

import com.sparta.sportify.dto.teamDto.ApproveRequestDto;
import com.sparta.sportify.dto.teamDto.ApproveResponseDto;
import com.sparta.sportify.dto.teamDto.TeamMemberResponseDto;
import com.sparta.sportify.entity.Team;
import com.sparta.sportify.entity.TeamMember;
import com.sparta.sportify.entity.TeamMemberRole;
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

    @Transactional
    public ApproveResponseDto approveOrRejectApplication(Long teamId, Long approveId, ApproveRequestDto requestDto) {
        // 팀과 사용자 객체 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다"));
        User approveUser = userRepository.findById(approveId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        User applyUser = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("신청자를 찾을 수 없습니다"));

        TeamMember approveMember = teamMemberRepository.findByUserAndTeam(approveUser, team)
                .orElseThrow(() -> new IllegalArgumentException("팀원이 아닙니다"));
        if (approveMember.getTeamMemberRole() != TeamMemberRole.TEAM_OWNER &&
                approveMember.getTeamMemberRole() != TeamMemberRole.MANAGER) {
            throw new IllegalStateException("신청을 승인하거나 거부할 권한이 없습니다.");
        }


        // 신청 상태 확인
        TeamMember teamMember = teamMemberRepository.findByUserAndTeam(applyUser, team)
                .orElseThrow(() -> new IllegalArgumentException("신청 내역이 없습니다."));

        boolean isAlreadyApproved = teamMemberRepository.existsByUserAndTeamAndStatus(approveUser, team, TeamMember.Status.APPROVED);
        if (isAlreadyApproved) {
            throw new IllegalStateException("이미 해당 팀에 가입되어 있습니다");
        }

        if (requestDto.isApprove()) {
            // 승인 처리
            teamMember.setStatus(TeamMember.Status.APPROVED);
            teamMember.setTeamMemberRole(TeamMemberRole.USER);
        } else {
            // 거부 처리
            teamMember.setStatus(TeamMember.Status.REJECTED);
        }

        // 변경 사항 저장
        teamMemberRepository.save(teamMember);

        return new ApproveResponseDto(requestDto.getUserId(), requestDto.isApprove());
    }
}
