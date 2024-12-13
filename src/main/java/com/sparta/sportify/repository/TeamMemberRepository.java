package com.sparta.sportify.repository;

import com.sparta.sportify.entity.Team;
import com.sparta.sportify.entity.TeamMember;
import com.sparta.sportify.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    boolean existsByUserAndTeamAndStatus(User user, Team team, TeamMember.Status status);

    Optional<TeamMember> findByUserAndTeam(User user, Team team);

    Optional<TeamMember> findByUserIdAndTeamId(Long userid, Long teamId);

    Page<TeamMember> findByTeamIdAndDeletedAtIsNull(Long teamId, Pageable pageable);

    Object findByTeamId(Long teamId);

//    void approveTeamMember(TeamMember teamMember);

    void grantRole(TeamMember teamMember, String admin);
    //Page<TeamMember> findByUserIdAndStatus(Long id, TeamMember.Status status, Pageable pageable);
    @Query("SELECT t FROM TeamMember t WHERE t.user.id = :userId AND t.status = 'APPROVED' AND t.deletedAt IS NULL")
    Page<TeamMember> findTeams(Long userId, Pageable pageable);
//    void grantRole(TeamMember teamMember, String admin);
}
