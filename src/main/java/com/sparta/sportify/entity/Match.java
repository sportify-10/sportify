package com.sparta.sportify.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "matchs")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private Integer time;
    private Integer aTeamCount;
    private Integer bTeamCount;

    @ManyToOne
    @JoinColumn(name = "stadium_time_id", nullable = false)
    private StadiumTime stadiumTime;

    public void discountATeamCount(int count) {
        this.bTeamCount -= count;
    }
    public void discountBTeamCount(int count) {
        this.bTeamCount -= count;
    }

    public void addATeamCount(int count) {
        this.aTeamCount += count;
    }
    public void addBTeamCount(int count) {
        this.bTeamCount += count;
    }
}

