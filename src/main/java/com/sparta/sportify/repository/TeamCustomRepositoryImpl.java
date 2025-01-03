package com.sparta.sportify.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.sportify.entity.team.QTeam;
import com.sparta.sportify.entity.team.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class TeamCustomRepositoryImpl implements TeamCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Team> findAllWithFilters(String sportType, String skillLevel, String region, Pageable pageable) {
        QTeam team = QTeam.team;

        // 동적 필터 조건
        BooleanBuilder builder = new BooleanBuilder();
        if (sportType != null && !sportType.isEmpty()) {
            builder.and(team.sportType.eq(sportType));
        }
        if (skillLevel != null && !skillLevel.isEmpty()) {
            builder.and(team.skillLevel.eq(skillLevel));
        }
        if (region != null && !region.isEmpty()) {
            builder.and(team.region.eq(region));
        }

        // QueryDSL로 데이터 조회 (teamPoints 내림차순 정렬)
        List<Team> content = queryFactory
                .selectFrom(team)
                .where(builder)
                .orderBy(team.teamPoints.desc()) // 항상 teamPoints 기준으로 내림차순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 데이터 수 조회
        long total = queryFactory
                .select(team.count())
                .from(team)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
