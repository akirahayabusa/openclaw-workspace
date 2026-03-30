/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.repository;

import io.luoshen.admin.model.SessionConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Session 配置 Repository
 */
@Repository
public interface SessionConfigRepository extends JpaRepository<SessionConfigEntity, Long> {
    
    Optional<SessionConfigEntity> findBySessionId(String sessionId);
    
    List<SessionConfigEntity> findByUserId(String userId);
    
    List<SessionConfigEntity> findByAgentId(String agentId);
    
    List<SessionConfigEntity> findByStatus(String status);
    
    void deleteBySessionId(String sessionId);
}