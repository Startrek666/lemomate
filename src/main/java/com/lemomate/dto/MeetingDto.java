package com.lemomate.dto;

import com.lemomate.model.Meeting;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDto {
    
    private Long id;
    
    @NotBlank(message = "会议标题不能为空")
    @Size(max = 200, message = "会议标题长度不能超过200个字符")
    private String title;
    
    @Size(max = 500, message = "会议描述长度不能超过500个字符")
    private String description;
    
    private Long creatorId;
    private String creatorName;
    
    @NotNull(message = "团队ID不能为空")
    private Long teamId;
    private String teamName;
    
    private String roomName;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private boolean teamOnly;
    
    private Meeting.MeetingStatus status;
    
    private String meetingUrl;
}
