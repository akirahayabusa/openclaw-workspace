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
     * @deprecated 使用 category + tags + customType 替代
     */
    @Deprecated
    private String type;
    
    /**
     * 技能包路径（本地文件系统路径）
     * 如果是压缩包上传的技能，存储解压后的目录路径
     */
    @Column(name = "package_path")
    private String packagePath;
    
    /**
     * 技能来源: builtin, user_upload, marketplace
     */
    @Column(nullable = false)
    private String source = "user_upload";
    
    /**
     * 风险等级: low, medium, high
     */
    @Column(nullable = false)
    private String riskLevel = "medium";
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 校验和（SHA-256，用于验证完整性）
     */
    private String checksum;
    
    /**
     * 依赖的其他技能（JSON 数组）
     */
    @Column(columnDefinition = "TEXT")
    private String dependencies;
    
    /**
     * 是否包含可执行脚本
     */
    private Boolean hasScripts = false;
    
    /**
     * 脚本列表（JSON 数组，相对路径）
     */
    @Column(columnDefinition = "TEXT")
    private String scriptFiles;
    
    /**
     * 主分类（可选）
     * 如：dev, ops, data, integration
     */
    @Column(length = 50)
    private String category;
    
    /**
     * 子分类（可选）
     * 如：git, cicd, analysis
     */
    @Column(length = 50)
    private String subCategory;
    
    /**
     * 技能标签（JSON 数组）
     * 如：["github", "git", "api", "automation"]
     */
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 自定义类型（可选）
     * 如果预定义分类不满足，用户可以自定义
     */
    @Column(length = 100)
    private String customType;
    
    /**
     * 技能能力特征（JSON 对象）
     * 如：{"hasScripts": true, "hasAssets": true, "needsNetwork": false}
     */
    @Column(columnDefinition = "TEXT")
    private String capabilities;
    
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
        
        // 兼容旧数据：如果 type 不为空但没有 category，使用 type 作为 category
        if (type != null && category == null) {
            category = type;
        }
        
        // 默认值
        if (source == null) {
            source = "user_upload";
        }
        if (riskLevel == null) {
            riskLevel = "medium";
        }
        if (hasScripts == null) {
            hasScripts = false;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 获取有效的类型标识
     * 优先使用 customType，其次使用 category/subCategory，最后使用 type
     */
    public String getEffectiveType() {
        if (customType != null && !customType.isEmpty()) {
            return customType;
        }
        if (category != null && !category.isEmpty()) {
            if (subCategory != null && !subCategory.isEmpty()) {
                return category + "/" + subCategory;
            }
            return category;
        }
        return type;
    }
}