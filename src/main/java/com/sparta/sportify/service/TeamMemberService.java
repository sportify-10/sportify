package com.sparta.sportify.service;

import com.sparta.sportify.dto.teamDto.*;
import com.sparta.sportify.entity.Team;
import com.sparta.sportify.entity.TeamMember;
import com.sparta.sportify.entity.TeamMemberRole;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.UserRepository;
import com.sparta.sportify.security.UserDetailsImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamMemberService {
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Transactional
    public TeamMemberResponseDto applyToTeam(Long teamId, UserDetailsImpl authUser) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        User user = authUser.getUser();
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
    public ApproveResponseDto approveOrRejectApplication(Long teamId, UserDetailsImpl authUser, ApproveRequestDto requestDto) {
        // 팀과 사용자 객체 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다"));
        User approveUser = authUser.getUser();
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

        boolean isAlreadyApproved = teamMemberRepository.existsByUserAndTeamAndStatus(applyUser, team, TeamMember.Status.APPROVED);
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

    @Transactional
    public RoleResponseDto grantRole(Long teamId, RoleRequestDto requestDto, UserDetailsImpl authUser) {
        // 요청자의 권한 확인
        TeamMember requester = teamMemberRepository.findByUserIdAndTeamId(authUser.getUser().getId(), teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀원이 아닙니다"));
        if (requester.getTeamMemberRole() != TeamMemberRole.TEAM_OWNER) {
            throw new IllegalStateException("팀장이 아니므로 역할을 부여할 수 없습니다.");
        }
        TeamMember teamMember = teamMemberRepository.findByUserIdAndTeamId(requestDto.getUserId(), teamId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 팀 멤버가 아닙니다."));
        TeamMemberRole role;
        try {
            role = TeamMemberRole.valueOf(requestDto.getRole());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 역할입니다.");
        }

        teamMember.setTeamMemberRole(role);
        teamMemberRepository.save(teamMember);

        return new RoleResponseDto(requestDto.getUserId(), requestDto.getRole());
    }

    public TeamMemberResponsePage getAllTeamMembers(int page, int size, Long teamId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TeamMember> teamMembers = teamMemberRepository.findByTeamIdAndDeletedAtIsNull(teamId, pageable);
        return new TeamMemberResponsePage(teamMembers);
    }

    @Transactional
    public TeamMemberResponseDto rejectTeamMember(Long teamId, Long userId, UserDetailsImpl authUser) {
        // 요청자의 팀원 정보 확인
        Long requesterId = authUser.getUser().getId();
        TeamMember requester = teamMemberRepository.findByUserIdAndTeamId(requesterId, teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀원이 아닙니다"));

        // 요청자가 팀장인지 확인
        if (requester.getTeamMemberRole() != TeamMemberRole.TEAM_OWNER) {
            throw new IllegalStateException("팀장이 아니므로 팀원을 퇴출할 수 없습니다.");
        }

        // 퇴출 대상 팀원 조회
        TeamMember teamMember = teamMemberRepository.findByUserIdAndTeamId(userId, requester.getTeam().getId())
                .orElseThrow(() -> new IllegalArgumentException("퇴출 대상 팀원을 찾을 수 없습니다."));


        // 소프트 삭제 처리
        teamMember.softDelete();
        teamMemberRepository.save(teamMember);

        return new TeamMemberResponseDto(teamMember);
    }
}
