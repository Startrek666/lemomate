package com.lemomate.controller;

import com.lemomate.dto.ApiResponse;
import com.lemomate.dto.UserDto;
import com.lemomate.model.Team;
import com.lemomate.model.User;
import com.lemomate.model.UserRole;
import com.lemomate.repository.TeamRepository;
import com.lemomate.repository.UserRepository;
import com.lemomate.security.UserDetailsImpl;
import com.lemomate.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyAuthority('USER', 'TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }

        User user = userOptional.get();
        UserDto userDto = convertToDto(user);

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = users.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDtos);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody UserDto userDto) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }

        User user = userOptional.get();

        // 更新用户角色
        if (userDto.getRole() != null) {
            user.setRole(userDto.getRole());
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse(true, "用户角色已更新"));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "角色不能为空"));
        }
    }

    @PutMapping("/{id}/team")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<?> updateUserTeam(@PathVariable Long id, @RequestBody UserDto userDto) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }

        User user = userOptional.get();

        // 更新用户团队
        if (userDto.getTeamId() != null) {
            Optional<Team> teamOptional = teamRepository.findById(userDto.getTeamId());
            if (!teamOptional.isPresent()) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "团队不存在"));
            }

            user.setTeam(teamOptional.get());
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse(true, "用户团队已更新"));
        } else {
            // 移除用户团队
            user.setTeam(null);
            userRepository.save(user);
            return ResponseEntity.ok(new ApiResponse(true, "用户已从团队中移除"));
        }
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setAvatarUrl(user.getAvatarUrl());

        if (user.getTeam() != null) {
            dto.setTeamId(user.getTeam().getId());
            dto.setTeamName(user.getTeam().getTeamName());
        }

        return dto;
    }

    @PostMapping("/avatar")
    @PreAuthorize("hasAnyAuthority('USER', 'TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }

        User user = userOptional.get();

        // 存储文件
        String fileName = fileStorageService.storeFile(file);

        // 生成文件访问URL
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(fileName)
                .toUriString();

        // 更新用户头像URL
        user.setAvatarUrl(fileDownloadUri);
        userRepository.save(user);

        Map<String, String> response = new HashMap<>();
        response.put("avatarUrl", fileDownloadUri);

        return ResponseEntity.ok(new ApiResponse(true, "头像上传成功", response));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyAuthority('USER', 'TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserDto userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }

        User user = userOptional.get();

        // 更新用户信息
        if (userDto.getRealName() != null && !userDto.getRealName().isEmpty()) {
            user.setRealName(userDto.getRealName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isEmpty()) {
            // 检查邮箱是否已被其他用户使用
            if (!user.getEmail().equals(userDto.getEmail()) &&
                    userRepository.existsByEmail(userDto.getEmail())) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "邮箱已被使用"));
            }
            user.setEmail(userDto.getEmail());
        }

        userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "个人资料已更新", convertToDto(user)));
    }
}
