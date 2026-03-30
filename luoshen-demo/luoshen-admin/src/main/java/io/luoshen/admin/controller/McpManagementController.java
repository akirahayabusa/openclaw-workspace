/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.McpConfigEntity;
import io.luoshen.admin.service.McpDynamicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * MCP 工具管理 API
 */
@RestController
@RequestMapping("/api/admin/mcp")
@RequiredArgsConstructor
public class McpManagementController {
    
    private final McpDynamicService mcpDynamicService;
    
    @GetMapping
    public List<McpConfigEntity> listAll() {
        return mcpDynamicService.listAllConfigs();
    }
    
    @GetMapping("/{toolId}")
    public McpConfigEntity get(@PathVariable String toolId) {
        return mcpDynamicService.getConfig(toolId);
    }
    
    @PostMapping
    public McpConfigEntity create(@RequestBody McpConfigEntity config) {
        return mcpDynamicService.createMcpConfig(config);
    }
    
    @PutMapping("/{toolId}")
    public McpConfigEntity update(@PathVariable String toolId, @RequestBody McpConfigEntity config) {
        return mcpDynamicService.updateMcpConfig(toolId, config);
    }
    
    @DeleteMapping("/{toolId}")
    public ResponseEntity<Void> delete(@PathVariable String toolId) {
        mcpDynamicService.deleteMcpConfig(toolId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAll() {
        mcpDynamicService.refreshAllMcpTools();
        return ResponseEntity.ok("刷新成功");
    }
}