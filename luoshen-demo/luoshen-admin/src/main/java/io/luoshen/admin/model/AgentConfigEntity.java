/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Agent 配置实体
 * 
 * 存储在数据库中，支持动态创建和管理
 */
@Data
@Entity
@Table(name = "luoshen_agent_config")
public class AgentConfigEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Agent 唯一标识符
     */
    @Column(unique = true, nullable = false)
    private String agentId;
    
    /**
     * Agent 名称
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Agent 描述
     */
    private String description;
    
    /**
     * 系统提示词
     */
    @Column(columnDefinition = "TEXT")
    private String systemPrompt;
    
    /**
     * Agent 类型: leader, core, sub
     */
    @Column(nullable = false)
    private String type;
    
    /**
     * 模型名称
     */
    private String modelName;
    
    /**
     * 工具列表（JSON 格式）
     */
    @Column(columnDefinition = "TEXT")
    private String toolsJson;
    
    /**
     * 技能列表（JSON 格式）
     */
    @Column(columnDefinition = "TEXT")
    private String skillsJson;
    
    /**
     * 父 Agent ID（用于层级关系）
     */
    private String parentAgentId;
    
    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 创建者
     */
    private String createdBy;
    
    /**
     * 更新者
     */
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}