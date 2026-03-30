/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.AgentConfigEntity;
import io.luoshen.admin.service.AgentDynamicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Agent 管理 API
 */
@RestController
@RequestMapping("/api/admin/agents")
@RequiredArgsConstructor
public class AgentManagementController {
    
    private final AgentDynamicService agentDynamicService;
    
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
     * 创建 Agent 配置
     */
    @PostMapping
    public AgentConfigEntity create(@RequestBody AgentConfigEntity config) {
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
     */
    @DeleteMapping("/{agentId}")
    public ResponseEntity<Void> delete(@PathVariable String agentId) {
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