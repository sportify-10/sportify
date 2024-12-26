package com.sparta.sportify.entity.teamArticle;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import com.sparta.sportify.entity.team.Team;
import com.sparta.sportify.entity.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "team_article")
public class TeamArticle {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;

	private String content;

	@CreatedDate
	@Column(updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private LocalDateTime createAt;

	private LocalDateTime deletedAt;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	// @OneToMany(mappedBy = "article", cascade = CascadeType.REMOVE)
	// private List<Comment> comments = new ArrayList<>();

	public void updateOf(String title, String content, User user, Team team) {
		this.title = title;
		this.content = content;
		this.user = user;
		this.team = team;
	}

	public void deleteOf() {
		this.deletedAt = LocalDateTime.now();
	}
}
