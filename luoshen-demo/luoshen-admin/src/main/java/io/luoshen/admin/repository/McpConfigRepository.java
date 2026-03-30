/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.repository;

import io.luoshen.admin.model.McpConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MCP 配置 Repository
 */
@Repository
public interface McpConfigRepository extends JpaRepository<McpConfigEntity, Long> {
    
    Optional<McpConfigEntity> findByToolId(String toolId);
    
    List<McpConfigEntity> findByType(String type);
    
    List<McpConfigEntity> findByEnabled(Boolean enabled);
    
    List<McpConfigEntity> findByAgentId(String agentId);
    
    void deleteByToolId(String toolId);
}