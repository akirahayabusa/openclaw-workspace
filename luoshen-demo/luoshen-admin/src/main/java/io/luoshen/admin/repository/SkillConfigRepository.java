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
}