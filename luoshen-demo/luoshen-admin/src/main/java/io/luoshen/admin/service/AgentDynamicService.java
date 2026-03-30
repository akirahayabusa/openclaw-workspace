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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 配置管理服务
 * 
 * 核心功能：
 * 1. Agent 配置的 CRUD 操作
 * 2. 层级关系管理
 * 3. 树形结构查询
 * 
 * 层级结构：
 * - LEADER: 顶层主控 Agent（唯一）
 * - CORE: 核心业务 Agent（多个，直接隶属于 LEADER）
 * - SUB: 专项功能 Agent（多个，隶属于 CORE）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentDynamicService {
    
    private final AgentConfigRepository agentConfigRepository;
    
    /**
     * 创建 Agent 配置
     */
    @Transactional
    public AgentConfigEntity createAgentConfig(AgentConfigEntity config) {
        if (agentConfigRepository.existsByAgentId(config.getAgentId())) {
            throw new IllegalArgumentException("Agent ID 已存在: " + config.getAgentId());
        }
        
        // 校验层级关系
        validateHierarchy(config);
        
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
        
        // 校验层级关系
        validateHierarchy(existing);
        
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
     * 按类型获取 Agent 列表
     */
    public List<AgentConfigEntity> getByType(String type) {
        return agentConfigRepository.findByType(type);
    }
    
    /**
     * 获取子 Agent 列表
     */
    public List<AgentConfigEntity> getChildren(String parentAgentId) {
        return agentConfigRepository.findByParentAgentId(parentAgentId);
    }
    
    /**
     * 获取 Agent 树形结构
     * 
     * 返回格式：
     * {
     *   "leader": {...},
     *   "coreAgents": [
     *     {
     *       "agent": {...},
     *       "subAgents": [...]
     *     }
     *   ]
     * }
     */
    public Map<String, Object> getAgentTree() {
        Map<String, Object> tree = new LinkedHashMap<>();
        
        // 获取 Leader
        List<AgentConfigEntity> leaders = agentConfigRepository.findByType("LEADER");
        AgentConfigEntity leader = leaders.isEmpty() ? null : leaders.get(0);
        tree.put("leader", leader);
        
        // 获取 Core Agents
        List<AgentConfigEntity> coreAgents = agentConfigRepository.findByType("CORE");
        List<Map<String, Object>> coreAgentNodes = new ArrayList<>();
        
        for (AgentConfigEntity core : coreAgents) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("agent", core);
            
            // 获取该 Core Agent 的子 Agent
            List<AgentConfigEntity> subAgents = agentConfigRepository.findByParentAgentId(core.getAgentId());
            node.put("subAgents", subAgents);
            
            coreAgentNodes.add(node);
        }
        
        tree.put("coreAgents", coreAgentNodes);
        
        return tree;
    }
    
    /**
     * 刷新所有 Agent（通知其他服务重新加载配置）
     */
    public void refreshAllAgents() {
        log.info("标记刷新所有 Agent 配置");
    }
    
    /**
     * 校验层级关系
     */
    private void validateHierarchy(AgentConfigEntity config) {
        String type = config.getType();
        String parentId = config.getParentAgentId();
        
        if ("LEADER".equals(type)) {
            // Leader 没有父 Agent
            if (parentId != null) {
                log.warn("Leader Agent 不应该有父 Agent: {}", config.getAgentId());
            }
        } else if ("CORE".equals(type)) {
            // Core Agent 必须隶属于 Leader
            if (parentId == null) {
                throw new IllegalArgumentException("Core Agent 必须指定 parentAgentId（应指向 Leader Agent）");
            }
            // 验证父 Agent 是 Leader
            AgentConfigEntity parent = agentConfigRepository.findByAgentId(parentId).orElse(null);
            if (parent == null || !"LEADER".equals(parent.getType())) {
                throw new IllegalArgumentException("Core Agent 的父 Agent 必须是 Leader 类型");
            }
        } else if ("SUB".equals(type)) {
            // Sub Agent 必须隶属于 Core Agent
            if (parentId == null) {
                throw new IllegalArgumentException("Sub Agent 必须指定 parentAgentId");
            }
            // 验证父 Agent 是 Core
            AgentConfigEntity parent = agentConfigRepository.findByAgentId(parentId).orElse(null);
            if (parent == null || !"CORE".equals(parent.getType())) {
                throw new IllegalArgumentException("Sub Agent 的父 Agent 必须是 CORE 类型");
            }
        }
    }
}