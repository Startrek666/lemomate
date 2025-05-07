package com.lemomate.controller;

import com.lemomate.dto.ApiResponse;
import com.lemomate.dto.JwtResponse;
import com.lemomate.dto.LoginRequest;
import com.lemomate.dto.UserDto;
import com.lemomate.model.Team;
import com.lemomate.model.TeamApplication;
import com.lemomate.model.User;
import com.lemomate.model.UserRole;
import com.lemomate.repository.TeamApplicationRepository;
import com.lemomate.repository.TeamRepository;
import com.lemomate.repository.UserRepository;
import com.lemomate.security.JwtUtils;
import com.lemomate.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TeamRepository teamRepository;
    
    @Autowired
    TeamApplicationRepository teamApplicationRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(item -> item.getAuthority())
                .orElse("USER");

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getRealName(),
                userDetails.getEmail(),
                role,
                userDetails.getTeamId(),
                userDetails.getTeamName()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户名已被使用"));
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "邮箱已被使用"));
        }

        // 创建新用户
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setRealName(userDto.getRealName());
        user.setEmail(userDto.getEmail());
        user.setPassword(encoder.encode(userDto.getPassword()));
        user.setRole(UserRole.USER); // 默认为普通用户

        // 如果选择了团队，创建团队申请
        if (userDto.getTeamId() != null) {
            Optional<Team> teamOptional = teamRepository.findById(userDto.getTeamId());
            if (teamOptional.isPresent()) {
                // 保存用户
                User savedUser = userRepository.save(user);
                
                // 创建团队申请
                TeamApplication application = new TeamApplication();
                application.setUser(savedUser);
                application.setTeam(teamOptional.get());
                application.setStatus(TeamApplication.ApplicationStatus.PENDING);
                teamApplicationRepository.save(application);
                
                return ResponseEntity.ok(new ApiResponse(true, "注册成功，等待团队管理员审核"));
            } else {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "所选团队不存在"));
            }
        } else {
            // 保存用户
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse(true, "注册成功"));
        }
    }
}
