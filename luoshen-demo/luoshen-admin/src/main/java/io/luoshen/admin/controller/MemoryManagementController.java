/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.MemoryConfigEntity;
import io.luoshen.admin.service.MemoryManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Memory 管理 API
 */
@RestController
@RequestMapping("/api/admin/memories")
@RequiredArgsConstructor
public class MemoryManagementController {
    
    private final MemoryManagementService memoryManagementService;
    
    @GetMapping
    public List<MemoryConfigEntity> listAll() {
        return memoryManagementService.listAllMemories();
    }
    
    @GetMapping("/{memoryId}")
    public MemoryConfigEntity get(@PathVariable String memoryId) {
        return memoryManagementService.getMemory(memoryId);
    }
    
    @GetMapping("/session/{sessionId}")
    public List<MemoryConfigEntity> getBySession(@PathVariable String sessionId) {
        return memoryManagementService.getSessionMemories(sessionId);
    }
    
    @GetMapping("/agent/{agentId}")
    public List<MemoryConfigEntity> getByAgent(@PathVariable String agentId) {
        return memoryManagementService.getAgentMemories(agentId);
    }
    
    @GetMapping("/type/{type}")
    public List<MemoryConfigEntity> getByType(@PathVariable String type) {
        return memoryManagementService.getMemoriesByType(type);
    }
    
    @GetMapping("/search")
    public List<MemoryConfigEntity> search(@RequestParam String keyword) {
        return memoryManagementService.searchMemories(keyword);
    }
    
    @PostMapping
    public MemoryConfigEntity create(@RequestBody CreateMemoryRequest request) {
        return memoryManagementService.createMemory(
                request.getMemoryId(),
                request.getSessionId(),
                request.getAgentId(),
                request.getType(),
                request.getContent()
        );
    }
    
    @PutMapping("/{memoryId}/content")
    public MemoryConfigEntity updateContent(@PathVariable String memoryId, @RequestParam String content) {
        return memoryManagementService.updateMemoryContent(memoryId, content);
    }
    
    @PutMapping("/{memoryId}/importance")
    public MemoryConfigEntity updateImportance(@PathVariable String memoryId, @RequestParam Double importance) {
        return memoryManagementService.updateMemoryImportance(memoryId, importance);
    }
    
    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Void> delete(@PathVariable String memoryId) {
        memoryManagementService.deleteMemory(memoryId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/session/{sessionId}")
    public ResponseEntity<Void> deleteBySession(@PathVariable String sessionId) {
        memoryManagementService.deleteSessionMemories(sessionId);
        return ResponseEntity.ok().build();
    }
    
    @lombok.Data
    public static class CreateMemoryRequest {
        private String memoryId;
        private String sessionId;
        private String agentId;
        private String type;
        private String content;
    }
}