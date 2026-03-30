/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.AgentConfigEntity;
import io.luoshen.admin.service.AgentDynamicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent 管理 API
 * 
 * 层级结构：
 * - LEADER: 顶层主控 Agent（唯一）
 * - CORE: 核心业务 Agent（多个）
 * - SUB: 专项功能 Agent（多个）
 */
@RestController
@RequestMapping("/api/admin/agents")
public class AgentManagementController {
    
    private final AgentDynamicService agentDynamicService;
    
    @Autowired
    public AgentManagementController(@Lazy AgentDynamicService agentDynamicService) {
        this.agentDynamicService = agentDynamicService;
    }
    
    /**
     * 获取所有 Agent 配置
     */
    @GetMapping
    public List<AgentConfigEntity> listAll() {
        return agentDynamicService.listAllConfigs();
    }
    
    /**
     * 获取单个 Agent 配置
     */
    @GetMapping("/{agentId}")
    public AgentConfigEntity get(@PathVariable String agentId) {
        return agentDynamicService.getConfig(agentId);
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
    @GetMapping("/tree")
    public Map<String, Object> getAgentTree() {
        return agentDynamicService.getAgentTree();
    }
    
    /**
     * 获取指定 Agent 的子 Agent 列表
     */
    @GetMapping("/{agentId}/children")
    public List<AgentConfigEntity> getChildren(@PathVariable String agentId) {
        return agentDynamicService.getChildren(agentId);
    }
    
    /**
     * 创建 Agent 配置
     * 
     * 约束：
     * - LEADER 类型的 Agent 只能有一个
     * - CORE 和 SUB 必须指定 parentAgentId
     */
    @PostMapping
    public AgentConfigEntity create(@RequestBody AgentConfigEntity config) {
        // 校验：Leader 只能有一个
        if ("LEADER".equals(config.getType())) {
            List<AgentConfigEntity> leaders = agentDynamicService.getByType("LEADER");
            if (!leaders.isEmpty() && !leaders.get(0).getAgentId().equals(config.getAgentId())) {
                throw new IllegalArgumentException("系统中已存在 Leader Agent，每个系统只能有一个 Leader");
            }
        }
        return agentDynamicService.createAgentConfig(config);
    }
    
    /**
     * 更新 Agent 配置
     */
    @PutMapping("/{agentId}")
    public AgentConfigEntity update(@PathVariable String agentId, @RequestBody AgentConfigEntity config) {
        return agentDynamicService.updateAgentConfig(agentId, config);
    }
    
    /**
     * 删除 Agent 配置
     * 
     * 约束：
     * - 如果有子 Agent，则不能删除
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<Void> delete(@PathVariable String agentId) {
        List<AgentConfigEntity> children = agentDynamicService.getChildren(agentId);
        if (!children.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        agentDynamicService.deleteAgentConfig(agentId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 刷新所有 Agent（重新加载配置）
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAll() {
        agentDynamicService.refreshAllAgents();
        return ResponseEntity.ok("刷新成功");
    }
}