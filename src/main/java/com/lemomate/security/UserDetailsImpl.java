package com.lemomate.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lemomate.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String realName;
    private String email;
    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private Long teamId;
    private String teamName;

    public UserDetailsImpl(Long id, String username, String realName, String email, String password,
                           Collection<? extends GrantedAuthority> authorities, Long teamId, String teamName) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        Long teamId = null;
        String teamName = null;
        if (user.getTeam() != null) {
            teamId = user.getTeam().getId();
            teamName = user.getTeam().getTeamName();
        }

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                teamId,
                teamName
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public Long getTeamId() {
        return teamId;
    }
    
    public String getTeamName() {
        return teamName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
