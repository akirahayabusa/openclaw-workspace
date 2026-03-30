/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Skill 配置实体
 * 
 * 存储技能定义，支持动态加载
 */
@Data
@Entity
@Table(name = "luoshen_skill_config")
public class SkillConfigEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Skill 唯一标识符
     */
    @Column(unique = true, nullable = false)
    private String skillId;
    
    /**
     * Skill 名称
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Skill 描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * Skill 内容（Markdown 格式）
     */
    @Column(columnDefinition = "TEXT")
    private String content;
    
    /**
     * Skill 类型: tool, workflow, knowledge
     */
    @Column(nullable = false)
    private String type;
    
    /**
     * 所属 Agent ID（可选）
     */
    private String agentId;
    
    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;
    
    /**
     * 版本号
     */
    private String version;
    
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