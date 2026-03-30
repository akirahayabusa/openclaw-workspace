/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.repository;

import io.luoshen.admin.model.SkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Skill 仓库
 */
@Repository
public interface SkillRepository extends JpaRepository<SkillEntity, Long> {
    
    /**
     * 根据名称查找
     */
    SkillEntity findByName(String name);
    
    /**
     * 根据分类查找
     */
    java.util.List<SkillEntity> findByCategory(String category);
    
    /**
     * 根据启用状态查找
     */
    java.util.List<SkillEntity> findByEnabled(Boolean enabled);
}
