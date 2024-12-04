package com.sparta.sportify.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;
    private String password;
    private String oauthId;
    private String oauthProvider;
    private String region;
    private String gender;
    private Integer age;
    private Integer levelPoints;
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    private Integer cash;

    // Getters and Setters
}
