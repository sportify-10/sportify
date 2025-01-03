package com.sparta.sportify.service;

import com.sparta.sportify.dto.teamDto.req.TeamRequestDto;
import com.sparta.sportify.dto.teamDto.res.DeleteResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamResponseDto;
import com.sparta.sportify.dto.teamDto.res.TeamResponsePage;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {
    private static final Object TEAM_MATCH = "teamMatch";
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public TeamResponseDto createTeam(TeamRequestDto requestDto, Long creatorId) {
        Team team = Team.builder()
                .teamName(requestDto.getTeamName())
                .region(requestDto.getRegion())
                .activityTime(requestDto.getActivityTime())
                .skillLevel(requestDto.getSkillLevel())
                .sportType(requestDto.getSportType())
                .description(requestDto.getDescription())
                .build();

        Team savedTeam = teamRepository.save(team);
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new CustomApiException(ErrorCode.USER_NOT_FOUND));

        TeamMember teamMember = TeamMember.builder()
                .user(creator)
                .team(savedTeam)
                .teamMemberRole(TeamMemberRole.TEAM_OWNER)
                .status(TeamMember.Status.APPROVED)
                .build();

        teamMemberRepository.save(teamMember);

        return new TeamResponseDto(savedTeam);
    }

    @Cacheable(value = "teamsCache", key = "'teams_' + #sportType + '_' + #skillLevel + '_' + #region + '_' + #page + '_' + #size")
    public TeamResponsePage getAllTeams(int page, int size, String sportType, String skillLevel, String region) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Team> teams = teamRepository.findAllWithFilters(sportType, skillLevel, region, pageable);
        return new TeamResponsePage(teams);
    }

    @Cacheable(value = "teamCache", key = "#teamId") // 캐시에 저장
    public TeamResponseDto getTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new CustomApiException(ErrorCode.TEAM_NOT_FOUND)
        );
        return new TeamResponseDto(team);
    }

    @Transactional
    public TeamResponseDto updateTeam(Long teamId, TeamRequestDto requestDto, UserDetailsImpl authUser) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new CustomApiException(ErrorCode.TEAM_NOT_FOUND));
        TeamMember user = teamMemberRepository.findById(authUser.getUser().getId()).orElseThrow(
                () -> new CustomApiException(ErrorCode.USER_NOT_FOUND)
        );
        if (user.getTeamMemberRole() != TeamMemberRole.TEAM_OWNER) {
            throw new CustomApiException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
        team.updateData(
                requestDto.getTeamName(),
                requestDto.getRegion(),
                requestDto.getActivityTime(),
                requestDto.getSkillLevel(),
                requestDto.getSportType(),
                requestDto.getDescription()
        );
        teamRepository.save(team);

        return new TeamResponseDto(team);
    }

    @Transactional
    public DeleteResponseDto deleteTeam(Long teamId, UserDetailsImpl authUser) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new CustomApiException(ErrorCode.TEAM_NOT_FOUND));
        TeamMember user = teamMemberRepository.findById(authUser.getUser().getId()).orElseThrow(
                () -> new CustomApiException(ErrorCode.USER_NOT_FOUND)
        );
        if (user.getTeamMemberRole() != TeamMemberRole.TEAM_OWNER) {
            throw new CustomApiException(ErrorCode.INSUFFICIENT_PERMISSION);
        }
        team.softDelete();
        teamRepository.save(team);

        return new DeleteResponseDto(team);
    }
}
