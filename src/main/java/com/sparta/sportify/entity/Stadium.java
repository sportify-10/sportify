package com.sparta.sportify.entity;

import java.time.LocalDateTime;

import com.sparta.sportify.dto.stadium.request.StadiumCreateRequestDto;
import com.sparta.sportify.dto.stadium.request.StadiumUpdateRequestDto;
import com.sparta.sportify.security.UserDetailsImpl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "stadiums")
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stadiumName;
    private String location;
    private int aTeamCount;
    private int bTeamCount;
    private String description;
    private int price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StadiumStatus status;

    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Stadium(String stadiumName, String location,int aTeamCount,int bTeamCount, String description, int price, UserDetailsImpl userDetails) {
        this.stadiumName = stadiumName;
        this.location = location;
        this.aTeamCount = aTeamCount;
        this.bTeamCount = bTeamCount;
        this.description = description;
        this.price = price;
        this.status = StadiumStatus.PENDING;
        this.deletedAt = null;
        this.user = userDetails.getUser();
    }

    public static Stadium createOf(String stadiumName, String location, int aTeamCount, int bTeamCount, String description, int price, UserDetailsImpl userDetails) {
        return new Stadium(stadiumName, location, aTeamCount, bTeamCount, description, price, userDetails);
    }

    public void updateOf(String stadiumName, String location, int aTeamCount, int bTeamCount, String description, int price) {
        this.stadiumName = stadiumName;
        this.location = location;
        this.aTeamCount = aTeamCount;
        this.bTeamCount = bTeamCount;
        this.description = description;
        this.price = price;
    }

    public void deleteOf() {
        this.deletedAt = LocalDateTime.now();
    }
}

