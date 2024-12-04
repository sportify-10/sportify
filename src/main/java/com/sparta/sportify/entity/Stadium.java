package com.sparta.sportify.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "stadiums")
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stadiumName;
    private String location;
    private Integer aTeamCount;
    private Integer bTeamCount;
    private String time;
    private String description;
    private Integer price;
    private String status;
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Getters and Setters
}

