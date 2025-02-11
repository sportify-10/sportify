// package com.sparta.sportify.entity.teamArticle;
//
// import java.time.LocalDateTime;
//
// import org.springframework.data.annotation.CreatedDate;
//
// import jakarta.persistence.Column;
// import jakarta.persistence.Entity;
// import jakarta.persistence.GeneratedValue;
// import jakarta.persistence.GenerationType;
// import jakarta.persistence.Id;
// import jakarta.persistence.Table;
// import jakarta.persistence.Temporal;
// import jakarta.persistence.TemporalType;
// import lombok.AllArgsConstructor;
// import lombok.Builder;
// import lombok.Getter;
// import lombok.NoArgsConstructor;
//
// @Entity
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// @Getter
// @Table(name = "comment")
// public class Comment {
// 	@Id
// 	@GeneratedValue(strategy = GenerationType.IDENTITY)
// 	private Long id;
//
// 	private String title;
//
// 	private String content;
//
// 	@CreatedDate
// 	@Column(updatable = false)
// 	@Temporal(TemporalType.TIMESTAMP)
// 	private LocalDateTime createAt;
//
// 	private String userName;
//
// 	// @ManyToOne(fetch = FetchType.LAZY)
// 	// @JoinColumn(name = "article_id")
// 	// private TeamArticle TeamArticle;
// }
