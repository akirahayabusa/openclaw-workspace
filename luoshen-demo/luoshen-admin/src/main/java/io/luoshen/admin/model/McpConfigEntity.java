/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * MCP 工具配置实体
 * 
 * 存储 MCP 工具定义，支持动态注册
 */
@Data
@Entity
@Table(name = "luoshen_mcp_config")
public class McpConfigEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 工具唯一标识符
     */
    @Column(unique = true, nullable = false)
    private String toolId;
    
    /**
     * 工具名称
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * 工具描述
     */
    @Column(columnDefinition = "TEXT")
    private String description;
    
    /**
     * 工具类型: query, action, control
     */
    @Column(nullable = false)
    private String type;
    
    /**
     * 参数定义（JSON 格式）
     */
    @Column(columnDefinition = "TEXT")
    private String parametersJson;
    
    /**
     * 执行脚本/代码
     */
    @Column(columnDefinition = "TEXT")
    private String implementation;
    
    /**
     * MCP 服务端点（如果是远程 MCP）
     */
    private String endpoint;
    
    /**
     * 所属 Agent ID
     */
    private String agentId;
    
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