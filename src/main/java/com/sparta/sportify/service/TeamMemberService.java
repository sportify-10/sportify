package com.sparta.sportify.service;

import com.sparta.sportify.dto.teamDto.req.ApproveRequestDto;
import com.sparta.sportify.dto.teamDto.req.RoleRequestDto;
import com.sparta.sportify.dto.teamDto.res.ApproveResponseDto;
import com.sparta.sportify.dto.teamDto.res.RoleResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamMemberResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamMemberResponsePage;
import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.teamMember.TeamMember;
import com.sparta.sportify.entity.teamMember.TeamMemberRole;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.exception.CustomApiException;
import com.sparta.sportify.exception.ErrorCode;
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
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new CustomApiException(ErrorCode.TEAM_NOT_FOUND)
        );
        User user = authUser.getUser();
        boolean isAlreadyPending = teamMemberRepository.existsByUserAndTeamAndStatus(user, team, TeamMember.Status.PENDING);
        if (isAlreadyPending) {
            throw new CustomApiException(ErrorCode.ALREADY_PENDING);
        }
        // 승인된 상태(가입) 확인
        boolean isAlreadyApproved = teamMemberRepository.existsByUserAndTeamAndStatus(user, team, TeamMember.Status.APPROVED);
        if (isAlreadyApproved) {
            throw new CustomApiException(ErrorCode.ALREADY_MEMBER);
        }

        TeamMember teamMember = new TeamMember(user, team);
        teamMemberRepository.save(teamMember);
        return new TeamMemberResponseDto(teamMember);
    }

    @Transactional
    public ApproveResponseDto approveOrRejectApplication(Long teamId, UserDetailsImpl authUser, ApproveRequestDto requestDto) {
        // 팀과 사용자 객체 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.TEAM_NOT_FOUND));
        User approveUser = authUser.getUser();
        User applyUser = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        TeamMember approveMember = teamMemberRepository.findByUserAndTeam(approveUser, team)
                .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_TEAM_MEMBER));
        if (approveMember.getTeamMemberRole() != TeamMemberRole.TEAM_OWNER &&
                approveMember.getTeamMemberRole() != TeamMemberRole.MANAGER) {
            throw new CustomApiException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
        // 신청 상태 확인
        TeamMember teamMember = teamMemberRepository.findByUserAndTeam(applyUser, team)
                .orElseThrow(() -> new CustomApiException(ErrorCode.APPLICATION_NOT_FOUND));


        boolean isAlreadyApproved = teamMemberRepository.existsByUserAndTeamAndStatus(applyUser, team, TeamMember.Status.APPROVED);
        if (isAlreadyApproved) {
            throw new CustomApiException(ErrorCode.ALREADY_MEMBER);
        }

        if (requestDto.isApprove()) {
            teamMember.approve();
        } else {
            teamMember.reject();
        }

        // 변경 사항 저장
        teamMemberRepository.save(teamMember);

        return new ApproveResponseDto(requestDto.getUserId(), requestDto.isApprove());
    }

    @Transactional
    public RoleResponseDto grantRole(Long teamId, RoleRequestDto requestDto, UserDetailsImpl authUser) {
        // 요청자의 권한 확인
        TeamMember requester = teamMemberRepository.findByUserIdAndTeamId(authUser.getUser().getId(), teamId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_TEAM_MEMBER));
        if (requester.getTeamMemberRole() != TeamMemberRole.TEAM_OWNER) {
            throw new CustomApiException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
        TeamMember teamMember = teamMemberRepository.findByUserIdAndTeamId(requestDto.getUserId(), teamId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.NOT_TEAM_MEMBER));
        TeamMemberRole role = TeamMemberRole.valueOf(String.valueOf(requestDto.getRole()));

        teamMember.grantRole(role);
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
        TeamMember requester = teamMemberRepository.findByUserIdAndTeamId(requesterId, teamId).orElseThrow(
                () -> new CustomApiException(ErrorCode.NOT_TEAM_MEMBER)
        );

        // 요청자가 팀장인지 확인
        if (requester.getTeamMemberRole() != TeamMemberRole.TEAM_OWNER) {
            throw new CustomApiException(ErrorCode.INSUFFICIENT_PERMISSION);
        }

        // 퇴출 대상 팀원 조회
        TeamMember teamMember = teamMemberRepository.findByUserIdAndTeamId(userId, requester.getTeam().getId()).orElseThrow(
                () -> new CustomApiException(ErrorCode.NOT_TEAM_MEMBER)
        );


        // 소프트 삭제 처리
        teamMember.softDelete();
        teamMemberRepository.save(teamMember);

        return new TeamMemberResponseDto(teamMember);
    }
}
