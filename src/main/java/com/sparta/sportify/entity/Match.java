package com.sparta.sportify.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "match")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String time;
    private Integer aTeamCount;
    private Integer bTeamCount;

    @ManyToOne
    @JoinColumn(name = "stadium_time_id", nullable = false)
    private StadiumTime stadiumTime;

    // Getters and Setters
}

