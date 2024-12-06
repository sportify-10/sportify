package com.sparta.sportify.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matchs")
@NoArgsConstructor
@Getter
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

    public Integer getaTeamCount() {
        return aTeamCount;
    }

    public Integer getbTeamCount() {
        return bTeamCount;
    }

    // Getters and Setters
}