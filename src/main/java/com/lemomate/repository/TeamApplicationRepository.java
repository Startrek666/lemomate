package com.lemomate.repository;

import com.lemomate.model.Team;
import com.lemomate.model.TeamApplication;
import com.lemomate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    
    List<TeamApplication> findByTeamAndStatus(Team team, TeamApplication.ApplicationStatus status);
    
    Optional<TeamApplication> findByUserAndTeamAndStatus(User user, Team team, TeamApplication.ApplicationStatus status);
    
    boolean existsByUserAndTeamAndStatus(User user, Team team, TeamApplication.ApplicationStatus status);
}
