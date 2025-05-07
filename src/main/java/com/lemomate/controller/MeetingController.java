package com.lemomate.controller;

import com.lemomate.dto.ApiResponse;
import com.lemomate.dto.MeetingDto;
import com.lemomate.model.Meeting;
import com.lemomate.model.Team;
import com.lemomate.model.User;
import com.lemomate.model.UserRole;
import com.lemomate.repository.MeetingRepository;
import com.lemomate.repository.TeamRepository;
import com.lemomate.repository.UserRepository;
import com.lemomate.security.JwtUtils;
import com.lemomate.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/meetings")
public class MeetingController {

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${jitsi.domain}")
    private String jitsiDomain;

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> createMeeting(@Valid @RequestBody MeetingDto meetingDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }

        Optional<Team> teamOptional = teamRepository.findById(meetingDto.getTeamId());
        if (!teamOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "团队不存在"));
        }

        // 检查用户是否是该团队的管理员
        User user = userOptional.get();
        Team team = teamOptional.get();

        if (!user.getTeam().getId().equals(team.getId()) && user.getRole() != UserRole.PLATFORM_ADMIN) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您不是该团队的管理员"));
        }

        // 创建会议
        Meeting meeting = new Meeting();
        meeting.setTitle(meetingDto.getTitle());
        meeting.setDescription(meetingDto.getDescription());
        meeting.setCreator(user);
        meeting.setTeam(team);

        // 使用会议标题作为房间名的基础
        // 处理标题，移除特殊字符，并添加时间戳以确保唯一性
        String baseRoomName = meetingDto.getTitle()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "")
                .replaceAll("\\s+", "_");

        // 添加时间戳确保唯一性
        String roomName = baseRoomName + "_" + System.currentTimeMillis();

        // 如果房间名过长，截取一部分
        if (roomName.length() > 50) {
            roomName = roomName.substring(0, 45) + "_" + System.currentTimeMillis() % 10000;
        }

        meeting.setRoomName(roomName);

        // 设置会议时间
        meeting.setStartTime(meetingDto.getStartTime());

        // 如果没有设置结束时间，则默认为开始时间后6小时
        if (meetingDto.getEndTime() == null) {
            meeting.setEndTime(meetingDto.getStartTime().plusHours(6));
        } else {
            meeting.setEndTime(meetingDto.getEndTime());
        }

        meeting.setTeamOnly(meetingDto.isTeamOnly());

        // 设置会议状态
        if (meetingDto.getStartTime().isAfter(LocalDateTime.now())) {
            meeting.setStatus(Meeting.MeetingStatus.SCHEDULED);
        } else {
            meeting.setStatus(Meeting.MeetingStatus.ONGOING);
        }

        try {
            Meeting savedMeeting = meetingRepository.save(meeting);

            // 构建返回DTO
            MeetingDto responseDto = convertToDto(savedMeeting);

            return ResponseEntity.ok(new ApiResponse(true, "会议创建成功", responseDto));
        } catch (Exception e) {
            // 记录详细错误信息
            System.err.println("创建会议失败: " + e.getMessage());
            e.printStackTrace();

            // 返回错误信息
            return ResponseEntity.badRequest().body(new ApiResponse(false, "创建会议失败: " + e.getMessage()));
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyAuthority('USER', 'TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> getTeamMeetings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        if (userDetails.getTeamId() == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您不属于任何团队"));
        }

        Optional<Team> teamOptional = teamRepository.findById(userDetails.getTeamId());
        if (!teamOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "团队不存在"));
        }

        List<Meeting> meetings = meetingRepository.findByTeam(teamOptional.get());

        // 更新会议状态
        meetings.forEach(meeting -> {
            if (meeting.getStatus() == Meeting.MeetingStatus.SCHEDULED &&
                    meeting.getStartTime().isBefore(LocalDateTime.now())) {
                meeting.setStatus(Meeting.MeetingStatus.ONGOING);
                meetingRepository.save(meeting);
            } else if (meeting.getStatus() == Meeting.MeetingStatus.ONGOING &&
                    meeting.getEndTime() != null && meeting.getEndTime().isBefore(LocalDateTime.now())) {
                meeting.setStatus(Meeting.MeetingStatus.ENDED);
                meetingRepository.save(meeting);
            }
        });

        List<MeetingDto> meetingDtos = meetings.stream()
                .map(this::convertToDto)
                .sorted(Comparator.comparing(MeetingDto::getStartTime).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(meetingDtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('USER', 'TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> getMeeting(@PathVariable Long id) {
        Optional<Meeting> meetingOptional = meetingRepository.findById(id);
        if (!meetingOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "会议不存在"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Meeting meeting = meetingOptional.get();

        // 检查用户是否有权限查看该会议
        if (meeting.isTeamOnly() && (userDetails.getTeamId() == null ||
                !userDetails.getTeamId().equals(meeting.getTeam().getId()))) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您没有权限查看该会议"));
        }

        MeetingDto meetingDto = convertToDto(meeting);

        return ResponseEntity.ok(meetingDto);
    }

    @GetMapping("/join/{id}")
    public ResponseEntity<?> joinMeeting(@PathVariable Long id) {
        Optional<Meeting> meetingOptional = meetingRepository.findById(id);
        if (!meetingOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "会议不存在"));
        }

        Meeting meeting = meetingOptional.get();

        // 检查会议是否已结束
        if (meeting.getStatus() == Meeting.MeetingStatus.ENDED) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "会议已结束"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "请先登录"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }

        User user = userOptional.get();

        // 检查用户是否有权限参加该会议
        if (meeting.isTeamOnly() && (user.getTeam() == null ||
                !user.getTeam().getId().equals(meeting.getTeam().getId()))) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "该会议仅限团队成员参加"));
        }

        // 生成JWT令牌
        String jwtToken = jwtUtils.generateJitsiToken(user, meeting);

        // 构建会议URL
        String meetingUrl = "https://" + jitsiDomain + "/" + meeting.getRoomName() + "?jwt=" + jwtToken;

        Map<String, String> response = new HashMap<>();
        response.put("meetingUrl", meetingUrl);

        return ResponseEntity.ok(new ApiResponse(true, "成功生成会议链接", response));
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyAuthority('TEAM_ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<?> updateMeetingStatus(@PathVariable Long id, @RequestBody Map<String, String> statusMap) {
        Optional<Meeting> meetingOptional = meetingRepository.findById(id);
        if (!meetingOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "会议不存在"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Optional<User> userOptional = userRepository.findById(userDetails.getId());
        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "用户不存在"));
        }

        User user = userOptional.get();
        Meeting meeting = meetingOptional.get();

        // 检查用户是否有权限修改会议状态
        if (user.getRole() != UserRole.PLATFORM_ADMIN &&
                (user.getTeam() == null || !user.getTeam().getId().equals(meeting.getTeam().getId()))) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "您没有权限修改该会议状态"));
        }

        String statusStr = statusMap.get("status");
        if (statusStr == null || statusStr.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "状态不能为空"));
        }

        try {
            Meeting.MeetingStatus status = Meeting.MeetingStatus.valueOf(statusStr);
            meeting.setStatus(status);

            // 如果状态为已结束，则设置结束时间为当前时间
            if (status == Meeting.MeetingStatus.ENDED && meeting.getEndTime().isAfter(LocalDateTime.now())) {
                meeting.setEndTime(LocalDateTime.now());
            }

            Meeting savedMeeting = meetingRepository.save(meeting);
            MeetingDto responseDto = convertToDto(savedMeeting);

            return ResponseEntity.ok(new ApiResponse(true, "会议状态已更新", responseDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "无效的会议状态"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "更新会议状态失败: " + e.getMessage()));
        }
    }

    private MeetingDto convertToDto(Meeting meeting) {
        MeetingDto dto = new MeetingDto();
        dto.setId(meeting.getId());
        dto.setTitle(meeting.getTitle());
        dto.setDescription(meeting.getDescription());
        dto.setCreatorId(meeting.getCreator().getId());
        dto.setCreatorName(meeting.getCreator().getRealName());
        dto.setTeamId(meeting.getTeam().getId());
        dto.setTeamName(meeting.getTeam().getTeamName());
        dto.setRoomName(meeting.getRoomName());
        dto.setStartTime(meeting.getStartTime());
        dto.setEndTime(meeting.getEndTime());
        dto.setTeamOnly(meeting.isTeamOnly());
        dto.setStatus(meeting.getStatus());

        // 构建会议URL（指向会议加入页面）
        dto.setMeetingUrl("/meetings/join/" + meeting.getId());

        return dto;
    }
}
