/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.repository;

import io.luoshen.admin.model.AgentConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Agent 配置 Repository
 */
@Repository
public interface AgentConfigRepository extends JpaRepository<AgentConfigEntity, Long> {
    
    Optional<AgentConfigEntity> findByAgentId(String agentId);
    
    List<AgentConfigEntity> findByType(String type);
    
    List<AgentConfigEntity> findByEnabled(Boolean enabled);
    
    List<AgentConfigEntity> findByParentAgentId(String parentAgentId);
    
    void deleteByAgentId(String agentId);
    
    boolean existsByAgentId(String agentId);
}