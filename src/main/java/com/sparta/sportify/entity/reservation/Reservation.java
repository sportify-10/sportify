package com.sparta.sportify.entity.reservation;

import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.team.TeamColor;
import com.sparta.sportify.entity.user.User;
import com.sparta.sportify.entity.match.Match;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate reservationDate;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    private Long totalAmount;
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    private TeamColor teamColor;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_id", nullable = true)
    private Team team;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;


    public void markAsDeleted(){
        this.status = ReservationStatus.CANCELED;
        this.deletedAt = LocalDateTime.now();
    }
}
