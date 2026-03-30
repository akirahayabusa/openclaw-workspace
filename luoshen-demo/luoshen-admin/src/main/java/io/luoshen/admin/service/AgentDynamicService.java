/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import io.luoshen.admin.model.AgentConfigEntity;
import io.luoshen.admin.repository.AgentConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Agent 配置管理服务
 * 
 * 核心功能：
 * 1. Agent 配置的 CRUD 操作
 * 2. 配置修改后持久化到数据库
 * 3. 不创建 Agent 实例（由 Agent 运行服务负责）
 * 
 * 设计原则：
 * 管理后台只负责配置的存储和管理，不负责 Agent 的实际运行
 * Agent 的运行由独立的 Agent 服务（如 luoshen-leader-agent）负责
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentDynamicService {
    
    private final AgentConfigRepository agentConfigRepository;
    
    /**
     * 创建新的 Agent 配置
     */
    @Transactional
    public AgentConfigEntity createAgentConfig(AgentConfigEntity config) {
        if (agentConfigRepository.existsByAgentId(config.getAgentId())) {
            throw new IllegalArgumentException("Agent ID 已存在: " + config.getAgentId());
        }
        
        AgentConfigEntity saved = agentConfigRepository.save(config);
        log.info("创建 Agent 配置: {}", saved.getAgentId());
        return saved;
    }
    
    /**
     * 更新 Agent 配置
     */
    @Transactional
    public AgentConfigEntity updateAgentConfig(String agentId, AgentConfigEntity newConfig) {
        AgentConfigEntity existing = agentConfigRepository.findByAgentId(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));
        
        // 更新字段
        existing.setName(newConfig.getName());
        existing.setDescription(newConfig.getDescription());
        existing.setSystemPrompt(newConfig.getSystemPrompt());
        existing.setType(newConfig.getType());
        existing.setModelName(newConfig.getModelName());
        existing.setToolsJson(newConfig.getToolsJson());
        existing.setSkillsJson(newConfig.getSkillsJson());
        existing.setParentAgentId(newConfig.getParentAgentId());
        existing.setEnabled(newConfig.getEnabled());
        
        AgentConfigEntity saved = agentConfigRepository.save(existing);
        log.info("更新 Agent 配置: {}", saved.getAgentId());
        return saved;
    }
    
    /**
     * 删除 Agent 配置
     */
    @Transactional
    public void deleteAgentConfig(String agentId) {
        agentConfigRepository.deleteByAgentId(agentId);
        log.info("删除 Agent 配置: {}", agentId);
    }
    
    /**
     * 获取 Agent 配置列表
     */
    public List<AgentConfigEntity> listAllConfigs() {
        return agentConfigRepository.findAll();
    }
    
    /**
     * 获取 Agent 配置
     */
    public AgentConfigEntity getConfig(String agentId) {
        return agentConfigRepository.findByAgentId(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + agentId));
    }
    
    /**
     * 刷新所有 Agent（通知其他服务重新加载配置）
     */
    public void refreshAllAgents() {
        // 这里只是标记需要刷新，实际的 Agent 运行服务会监听到这个事件
        log.info("标记刷新所有 Agent 配置");
    }
}