/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Memory 配置实体
 * 
 * 存储记忆信息，支持记忆管理
 */
@Data
@Entity
@Table(name = "luoshen_memory_config")
public class MemoryConfigEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Memory 唯一标识符
     */
    @Column(unique = true, nullable = false)
    private String memoryId;
    
    /**
     * Session ID
     */
    @Column(nullable = false)
    private String sessionId;
    
    /**
     * Agent ID
     */
    @Column(nullable = false)
    private String agentId;
    
    /**
     * Memory 类型: short_term, long_term, semantic
     */
    @Column(nullable = false)
    private String type;
    
    /**
     * Memory 内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * 关键词/标签（JSON 格式）
     */
    @Column(columnDefinition = "TEXT")
    private String tagsJson;
    
    /**
     * 重要性评分
     */
    private Double importance;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessedAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastAccessedAt = LocalDateTime.now();
    }
}