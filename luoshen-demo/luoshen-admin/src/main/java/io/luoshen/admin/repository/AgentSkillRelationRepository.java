/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.repository;

import io.luoshen.admin.model.AgentSkillRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agent 技能关联 Repository
 */
@Repository
public interface AgentSkillRelationRepository extends JpaRepository<AgentSkillRelationEntity, Long> {

    /**
     * 查询 Agent 的所有技能绑定关系
     */
    List<AgentSkillRelationEntity> findByAgentId(String agentId);

    /**
     * 查询 Agent 的启用的技能绑定关系
     */
    List<AgentSkillRelationEntity> findByAgentIdAndEnabled(String agentId, Boolean enabled);

    /**
     * 查询 Agent 的特定技能绑定关系
     */
    Optional<AgentSkillRelationEntity> findByAgentIdAndSkillId(String agentId, String skillId);

    /**
     * 查询使用某技能的所有 Agent
     */
    List<AgentSkillRelationEntity> findBySkillId(String skillId);

    /**
     * 删除 Agent 的所有技能绑定
     */
    void deleteByAgentId(String agentId);

    /**
     * 删除 Agent 的特定技能绑定
     */
    void deleteByAgentIdAndSkillId(String agentId, String skillId);

    /**
     * 检查 Agent 是否绑定了某技能
     */
    boolean existsByAgentIdAndSkillId(String agentId, String skillId);
}
