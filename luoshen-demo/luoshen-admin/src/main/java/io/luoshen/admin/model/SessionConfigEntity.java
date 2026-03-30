/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Session 配置实体
 * 
 * 存储会话信息，支持会话管理
 */
@Data
@Entity
@Table(name = "luoshen_session_config")
public class SessionConfigEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Session 唯一标识符
     */
    @Column(unique = true, nullable = false)
    private String sessionId;
    
    /**
     * 用户 ID
     */
    @Column(nullable = false)
    private String userId;
    
    /**
     * Agent ID
     */
    @Column(nullable = false)
    private String agentId;
    
    /**
     * Session 状态: active, paused, closed
     */
    @Column(nullable = false)
    private String status;
    
    /**
     * Session 数据（JSON 格式）
     */
    @Column(columnDefinition = "TEXT")
    private String sessionData;
    
    /**
     * 消息数量
     */
    private Integer messageCount;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 最后活跃时间
     */
    @Column(nullable = false)
    private LocalDateTime lastActiveAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
        if (status == null) {
            status = "active";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastActiveAt = LocalDateTime.now();
    }
}