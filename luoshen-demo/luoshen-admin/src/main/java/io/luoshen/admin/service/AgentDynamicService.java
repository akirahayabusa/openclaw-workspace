/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Toolkit;
import io.luoshen.admin.model.AgentConfigEntity;
import io.luoshen.admin.repository.AgentConfigRepository;
import io.luoshen.core.spec.AgentSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 动态管理服务
 * 
 * 核心功能：
 * 1. 从数据库读取配置，动态创建 Agent
 * 2. 配置修改后即时生效（无需重启）
 * 3. Agent 实例缓存管理
 */
@Slf4j
@Service
public class AgentDynamicService {
    
    private final AgentConfigRepository agentConfigRepository;
    private final Model model;
    private final Map<String, Object> availableTools;
    private final Map<String, AgentSkill> availableSkills;
    
    // Agent 实例缓存
    private final Map<String, ReActAgent> agentCache = new ConcurrentHashMap<>();
    
    public AgentDynamicService(
            AgentConfigRepository agentConfigRepository,
            Model model,
            Map<String, Object> availableTools,
            Map<String, AgentSkill> availableSkills) {
        this.agentConfigRepository = agentConfigRepository;
        this.model = model;
        this.availableTools = availableTools;
        this.availableSkills = availableSkills;
    }
    
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
        
        // 立即创建 Agent 实例
        createAgentInstance(saved);
        
        return saved;
    }
    
    /**
     * 更新 Agent 配置
     * 
     * 更新后立即重新创建 Agent 实例，配置即时生效
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
        
        // 清除旧实例，重新创建
        agentCache.remove(agentId);
        if (saved.getEnabled()) {
            createAgentInstance(saved);
        }
        
        return saved;
    }
    
    /**
     * 删除 Agent 配置
     */
    @Transactional
    public void deleteAgentConfig(String agentId) {
        agentCache.remove(agentId);
        agentConfigRepository.deleteByAgentId(agentId);
        log.info("删除 Agent 配置: {}", agentId);
    }
    
    /**
     * 获取 Agent 实例
     * 
     * 如果缓存中没有，从数据库加载配置并创建
     */
    public ReActAgent getAgent(String agentId) {
        return agentCache.computeIfAbsent(agentId, id -> {
            AgentConfigEntity config = agentConfigRepository.findByAgentId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Agent 不存在: " + id));
            
            if (!config.getEnabled()) {
                throw new IllegalStateException("Agent 已禁用: " + id);
            }
            
            return createAgentInstance(config);
        });
    }
    
    /**
     * 获取所有启用的 Agent
     */
    public List<ReActAgent> getAllEnabledAgents() {
        List<AgentConfigEntity> configs = agentConfigRepository.findByEnabled(true);
        return configs.stream()
                .map(c -> getAgent(c.getAgentId()))
                .toList();
    }
    
    /**
     * 刷新所有 Agent（重新从数据库加载）
     */
    public void refreshAllAgents() {
        agentCache.clear();
        List<AgentConfigEntity> configs = agentConfigRepository.findByEnabled(true);
        configs.forEach(this::createAgentInstance);
        log.info("刷新所有 Agent，共 {} 个", configs.size());
    }
    
    /**
     * 根据 AgentSpec 创建 Agent 实例
     */
    private ReActAgent createAgentInstance(AgentConfigEntity config) {
        // 解析工具和技能
        List<String> toolNames = parseJsonList(config.getToolsJson());
        List<String> skillNames = parseJsonList(config.getSkillsJson());
        
        // 创建 Toolkit
        Toolkit toolkit = new Toolkit();
        for (String toolName : toolNames) {
            Object tool = availableTools.get(toolName);
            if (tool != null) {
                toolkit.registerTool(tool);
            } else {
                log.warn("工具不存在: {}", toolName);
            }
        }
        
        // 创建 SkillBox
        SkillBox skillBox = new SkillBox(toolkit);
        for (String skillName : skillNames) {
            AgentSkill skill = availableSkills.get(skillName);
            if (skill != null) {
                skillBox.registration().skill(skill).apply();
            } else {
                log.warn("技能不存在: {}", skillName);
            }
        }
        
        // 创建 Agent
        ReActAgent agent = ReActAgent.builder()
                .name(config.getName())
                .description(config.getDescription())
                .sysPrompt(config.getSystemPrompt() != null ? config.getSystemPrompt() : "")
                .model(model)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .memory(new InMemoryMemory())
                .build();
        
        agentCache.put(config.getAgentId(), agent);
        log.info("创建 Agent 实例: {}", config.getAgentId());
        
        return agent;
    }
    
    /**
     * 解析 JSON 列表
     */
    private List<String> parseJsonList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        // 简单解析，实际应使用 JSON 解析器
        try {
            return List.of(json.split(","));
        } catch (Exception e) {
            return List.of();
        }
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
}