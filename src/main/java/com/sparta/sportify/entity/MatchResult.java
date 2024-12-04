package com.sparta.sportify.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "match_result")
public class MatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchResultId;

    private Integer teamAScore;
    private Integer teamBScore;

    @Enumerated(EnumType.STRING)
    private MatchStatus matchStatus;

    private LocalDate matchDate;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    // Getters and Setters
}
