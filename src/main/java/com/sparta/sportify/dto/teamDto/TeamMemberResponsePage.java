package com.sparta.sportify.dto.teamDto;

import com.sparta.sportify.entity.TeamMember;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class TeamMemberResponsePage {
    private List<TeamMemberResponseDto> teamMembers;
    private int totalPages;
    private long totalElements;

    public TeamMemberResponsePage(Page<TeamMember> page) {
        this.teamMembers = page.getContent().stream()
                .map(TeamMemberResponseDto::new)
                .collect(Collectors.toList());
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }
}
