package com.sparta.sportify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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

    public enum Status{
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
    // Getters and Setters
}
