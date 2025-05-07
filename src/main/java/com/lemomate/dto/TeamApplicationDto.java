package com.lemomate.dto;

import com.lemomate.model.TeamApplication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamApplicationDto {
    
    private Long id;
    private Long userId;
    private String username;
    private String realName;
    private String email;
    private Long teamId;
    private String teamName;
    private TeamApplication.ApplicationStatus status;
    private LocalDateTime applyTime;
    private LocalDateTime processTime;
}
