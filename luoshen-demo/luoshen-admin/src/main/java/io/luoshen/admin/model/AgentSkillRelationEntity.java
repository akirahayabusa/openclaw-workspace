/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Agent 技能关联实体
 *
 * 定义 Agent 和 Skill 的多对多关系
 */
@Data
@Entity
@Table(name = "luoshen_agent_skill_relation")
public class AgentSkillRelationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Agent ID
     */
    @Column(nullable = false)
    private String agentId;

    /**
     * Skill ID
     */
    @Column(nullable = false)
    private String skillId;

    /**
     * 绑定模式: inherit, custom, none
     * - inherit: 继承父级技能
     * - custom: 使用自定义技能列表
     * - none: 禁用所有技能
     */
    @Column(nullable = false)
    private String bindMode = "custom";

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled = true;

    /**
     * 优先级（数字越小优先级越高）
     * 用于控制技能加载顺序
     */
    private Integer priority = 0;

    /**
     * 配置参数（JSON 格式）
     * 用于存储技能特定的配置
     */
    @Column(columnDefinition = "TEXT")
    private String config;

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
