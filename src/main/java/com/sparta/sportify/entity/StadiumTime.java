package com.sparta.sportify.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "stadium_times")
public class StadiumTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cron;

    @ManyToOne
    @JoinColumn(name = "stadium_id", nullable = false)
    private Stadium stadium;

    // Getters and Setters
}

