package com.lemomate.repository;

import com.lemomate.model.Meeting;
import com.lemomate.model.Team;
import com.lemomate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    
    List<Meeting> findByTeam(Team team);
    
    List<Meeting> findByCreator(User creator);
    
    List<Meeting> findByTeamAndStartTimeAfter(Team team, LocalDateTime startTime);
    
    Optional<Meeting> findByRoomName(String roomName);
}
