package com.sparta.sportify.entity.teamMember;

import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "team_members")
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamMemberId;

    @Enumerated(EnumType.STRING)
    private TeamMemberRole teamMemberRole;

    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    public TeamMember(User user, Team team) {
        this.user = user;
        this.team = team;
        this.status = Status.PENDING; // 기본 상태를 대기 상태로 설정
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = Status.APPROVED;
        this.teamMemberRole = TeamMemberRole.USER;
    }

    public void reject() {
        this.status = Status.REJECTED;
    }

    public void grantRole(TeamMemberRole role) {
        this.teamMemberRole = role;
    }
    // Getters and Setters
}
