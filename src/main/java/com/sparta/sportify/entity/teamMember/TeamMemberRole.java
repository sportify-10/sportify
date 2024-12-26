package com.sparta.sportify.entity.teamMember;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum TeamMemberRole {
    USER("유저"),MANAGER("관리자"),TEAM_OWNER("팀장");

    private final String value;
}
