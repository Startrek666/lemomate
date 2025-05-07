package com.lemomate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamDto {
    
    private Long id;
    
    @NotBlank(message = "团队名称不能为空")
    @Size(min = 2, max = 100, message = "团队名称长度必须在2-100个字符之间")
    private String teamName;
    
    private int memberCount;
}
