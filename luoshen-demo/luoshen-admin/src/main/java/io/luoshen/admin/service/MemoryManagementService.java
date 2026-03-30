/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import io.luoshen.admin.model.MemoryConfigEntity;
import io.luoshen.admin.repository.MemoryConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Memory 管理服务
 * 
 * 核心功能：
 * 1. Memory 创建、查询、删除
 * 2. Memory 类型管理（短期/长期/语义）
 * 3. Memory 重要性评分
 */
@Slf4j
@Service
public class MemoryManagementService {
    
    private final MemoryConfigRepository memoryConfigRepository;
    
    public MemoryManagementService(MemoryConfigRepository memoryConfigRepository) {
        this.memoryConfigRepository = memoryConfigRepository;
    }
    
    /**
     * 创建 Memory
     */
    @Transactional
    public MemoryConfigEntity createMemory(String memoryId, String sessionId, String agentId, 
                                           String type, String content) {
        MemoryConfigEntity memory = new MemoryConfigEntity();
        memory.setMemoryId(memoryId);
        memory.setSessionId(sessionId);
        memory.setAgentId(agentId);
        memory.setType(type);
        memory.setContent(content);
        memory.setImportance(0.5); // 默认重要性
        
        return memoryConfigRepository.save(memory);
    }
    
    /**
     * 获取 Memory
     */
    public MemoryConfigEntity getMemory(String memoryId) {
        return memoryConfigRepository.findByMemoryId(memoryId)
                .orElseThrow(() -> new IllegalArgumentException("Memory 不存在: " + memoryId));
    }
    
    /**
     * 获取 Session 的所有 Memory
     */
    public List<MemoryConfigEntity> getSessionMemories(String sessionId) {
        return memoryConfigRepository.findBySessionId(sessionId);
    }
    
    /**
     * 获取 Agent 的所有 Memory
     */
    public List<MemoryConfigEntity> getAgentMemories(String agentId) {
        return memoryConfigRepository.findByAgentId(agentId);
    }
    
    /**
     * 按类型获取 Memory
     */
    public List<MemoryConfigEntity> getMemoriesByType(String type) {
        return memoryConfigRepository.findByType(type);
    }
    
    /**
     * 更新 Memory 内容
     */
    @Transactional
    public MemoryConfigEntity updateMemoryContent(String memoryId, String content) {
        MemoryConfigEntity memory = getMemory(memoryId);
        memory.setContent(content);
        return memoryConfigRepository.save(memory);
    }
    
    /**
     * 更新 Memory 重要性
     */
    @Transactional
    public MemoryConfigEntity updateMemoryImportance(String memoryId, Double importance) {
        MemoryConfigEntity memory = getMemory(memoryId);
        memory.setImportance(importance);
        return memoryConfigRepository.save(memory);
    }
    
    /**
     * 删除 Memory
     */
    @Transactional
    public void deleteMemory(String memoryId) {
        memoryConfigRepository.deleteByMemoryId(memoryId);
        log.info("删除 Memory: {}", memoryId);
    }
    
    /**
     * 删除 Session 的所有 Memory
     */
    @Transactional
    public void deleteSessionMemories(String sessionId) {
        List<MemoryConfigEntity> memories = memoryConfigRepository.findBySessionId(sessionId);
        memories.forEach(m -> memoryConfigRepository.deleteByMemoryId(m.getMemoryId()));
        log.info("删除 Session {} 的所有 Memory: {} 个", sessionId, memories.size());
    }
    
    /**
     * 获取所有 Memory
     */
    public List<MemoryConfigEntity> listAllMemories() {
        return memoryConfigRepository.findAll();
    }
    
    /**
     * 搜索 Memory（按内容关键词）
     */
    public List<MemoryConfigEntity> searchMemories(String keyword) {
        return memoryConfigRepository.findAll().stream()
                .filter(m -> m.getContent() != null && m.getContent().contains(keyword))
                .toList();
    }
}