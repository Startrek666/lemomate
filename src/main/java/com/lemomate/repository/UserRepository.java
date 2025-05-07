package com.lemomate.repository;

import com.lemomate.model.Team;
import com.lemomate.model.User;
import com.lemomate.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findByTeam(Team team);
    
    List<User> findByTeamAndRole(Team team, UserRole role);
}
