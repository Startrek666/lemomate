package com.lemomate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_applications")
public class TeamApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;
    
    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;
    
    @Column(name = "process_time")
    private LocalDateTime processTime;
    
    @PrePersist
    protected void onCreate() {
        applyTime = LocalDateTime.now();
    }
    
    public enum ApplicationStatus {
        PENDING,    // 待审核
        APPROVED,   // 已通过
        REJECTED    // 已拒绝
    }
}
