package com.lemomate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String role;
    private Long teamId;
    private String teamName;
    
    public JwtResponse(String token, Long id, String username, String realName, String email, String role, Long teamId, String teamName) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.role = role;
        this.teamId = teamId;
        this.teamName = teamName;
    }
}
