package com.sparta.sportify.dto.teamDto;

import com.sparta.sportify.entity.Team;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponsePage {
    private List<TeamResponseDto> teams;
    private int totalPages;
    private long totalElements;

    public TeamResponsePage(Page<Team> page) {
        this.teams = page.getContent().stream()
                .map(TeamResponseDto::new)
                .collect(Collectors.toList());
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }
}
