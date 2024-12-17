package com.sparta.sportify.service;

import com.sparta.sportify.dto.teamDto.TeamRequestDto;
import com.sparta.sportify.dto.teamDto.TeamResponseDto;
import com.sparta.sportify.dto.teamDto.TeamResponsePage;
import com.sparta.sportify.entity.Team;
import com.sparta.sportify.entity.TeamMember;
import com.sparta.sportify.entity.TeamMemberRole;
import com.sparta.sportify.entity.User;
import com.sparta.sportify.repository.TeamMemberRepository;
import com.sparta.sportify.repository.TeamRepository;
import com.sparta.sportify.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamService {
    private static final Object TEAM_MATCH = "teamMatch";
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    // private final RedisTemplate<String, Object> redisTemplate;

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
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을수 없습니다"));
        TeamMember teamMember = new TeamMember(creator, savedTeam);
        teamMember.setTeamMemberRole(TeamMemberRole.TEAM_OWNER); // 팀장 역할
        teamMember.setStatus(TeamMember.Status.APPROVED); // 자동 승인
        teamMemberRepository.save(teamMember);

        return new TeamResponseDto(savedTeam);
    }

    public TeamResponsePage getAllTeams(int page, int size, String sportType, String skillLevel, String region) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Team> teams = teamRepository.findAllWithFilters(sportType, skillLevel, region, pageable);
        return new TeamResponsePage(teams);
    }

    @Cacheable(value = "teamCache", key = "#teamId") // 캐시에 저장
    public TeamResponseDto getTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        return new TeamResponseDto(team);
    }

    @Transactional
    public TeamResponseDto updateTeam(Long teamId, TeamRequestDto requestDto) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다.: " + teamId));
        team.updateData(requestDto.getTeamName(),requestDto.getRegion() , requestDto.getActivityTime(),requestDto.getSkillLevel() ,requestDto.getSportType(), requestDto.getDescription());

        return new TeamResponseDto(team);
    }

    @Transactional
    public TeamResponseDto deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다.: " + teamId));
        team.softDelete();
        teamRepository.save(team);

        return new TeamResponseDto(team);
    }
}
