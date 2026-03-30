/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.repository;

import io.luoshen.admin.model.MemoryConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Memory Repository (别名)
 */
@Repository
public interface MemoryRepository extends JpaRepository<MemoryConfigEntity, Long> {
    
    Optional<MemoryConfigEntity> findByMemoryId(String memoryId);
    
    List<MemoryConfigEntity> findBySessionId(String sessionId);
    
    List<MemoryConfigEntity> findByAgentId(String agentId);
    
    List<MemoryConfigEntity> findByType(String type);
    
    void deleteByMemoryId(String memoryId);
}
