package com.sparta.sportify.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "match_results")
public class MatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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