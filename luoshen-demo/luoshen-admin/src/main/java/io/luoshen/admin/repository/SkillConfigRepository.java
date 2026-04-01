/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.repository;

import io.luoshen.admin.model.SkillConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Skill 配置 Repository
 */
@Repository
public interface SkillConfigRepository extends JpaRepository<SkillConfigEntity, Long> {
    
    Optional<SkillConfigEntity> findBySkillId(String skillId);
    
    List<SkillConfigEntity> findByType(String type);

    List<SkillConfigEntity> findByEnabled(Boolean enabled);

    List<SkillConfigEntity> findByAgentId(String agentId);

    void deleteBySkillId(String skillId);

    // 新增查询方法

    /**
     * 按来源查询
     */
    List<SkillConfigEntity> findBySource(String source);

    /**
     * 按分类查询
     */
    List<SkillConfigEntity> findByCategory(String category);

    /**
     * 按分类和子分类查询
     */
    List<SkillConfigEntity> findByCategoryAndSubCategory(String category, String subCategory);

    /**
     * 按风险等级查询
     */
    List<SkillConfigEntity> findByRiskLevel(String riskLevel);

    /**
     * 查询包含脚本的技能
     */
    List<SkillConfigEntity> findByHasScriptsTrue();

    /**
     * 按来源和启用状态查询
     */
    List<SkillConfigEntity> findBySourceAndEnabled(String source, Boolean enabled);

    /**
     * 检查技能是否存在
     */
    boolean existsBySkillId(String skillId);
}