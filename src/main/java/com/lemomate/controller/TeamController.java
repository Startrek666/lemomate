package com.lemomate.controller;

import com.lemomate.dto.ApiResponse;
import com.lemomate.dto.TeamApplicationDto;
import com.lemomate.dto.TeamDto;
import com.lemomate.dto.UserDto;
import com.lemomate.model.Team;
import com.lemomate.model.TeamApplication;
import com.lemomate.model.User;
import com.lemomate.model.UserRole;
import com.lemomate.repository.TeamApplicationRepository;
import com.lemomate.repository.TeamRepository;
import com.lemomate.repository.UserRepository;
import com.lemomate.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamApplicationRepository teamApplicationRepository;

    @GetMapping("/list")
    public ResponseEntity<List<TeamDto>> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        List<TeamDto> teamDtos = teams.stream().map(team -> {
            TeamDto dto = new TeamDto();
            dto.setId(team.getId());
            dto.setTeamName(team.getTeamName());
            dto.setMemberCount(team.getMembers().size());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(teamDtos);
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<?> createTeam(@Valid @RequestBody TeamDto teamDto) {
        if (teamRepository.existsByTeamName(teamDto.getTeamName())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "团队名称已存在"));
        }

        Team team = new Team();
        team.setTeamName(teamDto.getTeamName());
        Team savedTeam = teamRepository.save(team);

        TeamDto responseDto = new TeamDto();
        responseDto.setId(savedTeam.getId());
        responseDto.setTeamName(savedTeam.getTeamName());
        responseDto.setMemberCount(0);

        return ResponseEntity.ok(new ApiResponse(true, "团队创建成功", responseDto));
    }

    @GetMapping("/members")
    @PreAuthorize("hasAnyAuthority('USER', 'TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> getTeamMembers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (userDetails.getTeamId() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您不属于任何团队"));
        }
        
        Optional<Team> teamOptional = teamRepository.findById(userDetails.getTeamId());
        if (!teamOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "团队不存在"));
        }
        
        List<User> members = userRepository.findByTeam(teamOptional.get());
        List<UserDto> memberDtos = members.stream().map(member -> {
            UserDto dto = new UserDto();
            dto.setId(member.getId());
            dto.setUsername(member.getUsername());
            dto.setRealName(member.getRealName());
            dto.setEmail(member.getEmail());
            dto.setRole(member.getRole());
            dto.setAvatarUrl(member.getAvatarUrl());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(memberDtos);
    }

    @GetMapping("/applications")
    @PreAuthorize("hasAnyAuthority('TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> getTeamApplications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        if (userDetails.getTeamId() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您不属于任何团队"));
        }
        
        Optional<Team> teamOptional = teamRepository.findById(userDetails.getTeamId());
        if (!teamOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "团队不存在"));
        }
        
        List<TeamApplication> applications = teamApplicationRepository.findByTeamAndStatus(
                teamOptional.get(), TeamApplication.ApplicationStatus.PENDING);
        
        List<TeamApplicationDto> applicationDtos = applications.stream().map(app -> {
            TeamApplicationDto dto = new TeamApplicationDto();
            dto.setId(app.getId());
            dto.setUserId(app.getUser().getId());
            dto.setUsername(app.getUser().getUsername());
            dto.setRealName(app.getUser().getRealName());
            dto.setEmail(app.getUser().getEmail());
            dto.setTeamId(app.getTeam().getId());
            dto.setTeamName(app.getTeam().getTeamName());
            dto.setStatus(app.getStatus());
            dto.setApplyTime(app.getApplyTime());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(applicationDtos);
    }

    @PostMapping("/applications/{id}/approve")
    @PreAuthorize("hasAnyAuthority('TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> approveApplication(@PathVariable Long id) {
        Optional<TeamApplication> applicationOptional = teamApplicationRepository.findById(id);
        if (!applicationOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "申请不存在"));
        }
        
        TeamApplication application = applicationOptional.get();
        if (application.getStatus() != TeamApplication.ApplicationStatus.PENDING) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "该申请已处理"));
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 检查当前用户是否是该团队的管理员
        if (!userDetails.getTeamId().equals(application.getTeam().getId()) && 
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PLATFORM_ADMIN"))) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您没有权限处理该申请"));
        }
        
        // 更新用户团队
        User user = application.getUser();
        user.setTeam(application.getTeam());
        userRepository.save(user);
        
        // 更新申请状态
        application.setStatus(TeamApplication.ApplicationStatus.APPROVED);
        application.setProcessTime(LocalDateTime.now());
        teamApplicationRepository.save(application);
        
        return ResponseEntity.ok(new ApiResponse(true, "已批准用户加入团队"));
    }

    @PostMapping("/applications/{id}/reject")
    @PreAuthorize("hasAnyAuthority('TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> rejectApplication(@PathVariable Long id) {
        Optional<TeamApplication> applicationOptional = teamApplicationRepository.findById(id);
        if (!applicationOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "申请不存在"));
        }
        
        TeamApplication application = applicationOptional.get();
        if (application.getStatus() != TeamApplication.ApplicationStatus.PENDING) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "该申请已处理"));
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // 检查当前用户是否是该团队的管理员
        if (!userDetails.getTeamId().equals(application.getTeam().getId()) && 
                !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("PLATFORM_ADMIN"))) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您没有权限处理该申请"));
        }
        
        // 更新申请状态
        application.setStatus(TeamApplication.ApplicationStatus.REJECTED);
        application.setProcessTime(LocalDateTime.now());
        teamApplicationRepository.save(application);
        
        return ResponseEntity.ok(new ApiResponse(true, "已拒绝用户加入团队"));
    }

    @PostMapping("/apply/{teamId}")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<?> applyToTeam(@PathVariable Long teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        if (!teamOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "团队不存在"));
        }
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }
        
        User user = userOptional.get();
        Team team = teamOptional.get();
        
        // 检查用户是否已经属于某个团队
        if (user.getTeam() != null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您已经属于一个团队"));
        }
        
        // 检查是否已经有待处理的申请
        if (teamApplicationRepository.existsByUserAndTeamAndStatus(
                user, team, TeamApplication.ApplicationStatus.PENDING)) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您已经申请加入该团队，请等待审核"));
        }
        
        // 创建新申请
        TeamApplication application = new TeamApplication();
        application.setUser(user);
        application.setTeam(team);
        application.setStatus(TeamApplication.ApplicationStatus.PENDING);
        teamApplicationRepository.save(application);
        
        return ResponseEntity.ok(new ApiResponse(true, "申请已提交，等待团队管理员审核"));
    }
}
