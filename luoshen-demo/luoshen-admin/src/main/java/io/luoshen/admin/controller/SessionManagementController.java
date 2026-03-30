/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.SessionConfigEntity;
import io.luoshen.admin.service.SessionManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Session 管理 API
 */
@RestController
@RequestMapping("/api/admin/sessions")
@RequiredArgsConstructor
public class SessionManagementController {
    
    private final SessionManagementService sessionManagementService;
    
    @GetMapping
    public List<SessionConfigEntity> listAll() {
        return sessionManagementService.listAllSessions();
    }
    
    @GetMapping("/{sessionId}")
    public SessionConfigEntity get(@PathVariable String sessionId) {
        return sessionManagementService.getSession(sessionId);
    }
    
    @GetMapping("/user/{userId}")
    public List<SessionConfigEntity> getByUser(@PathVariable String userId) {
        return sessionManagementService.getUserSessions(userId);
    }
    
    @GetMapping("/agent/{agentId}")
    public List<SessionConfigEntity> getByAgent(@PathVariable String agentId) {
        return sessionManagementService.getAgentSessions(agentId);
    }
    
    @PostMapping
    public SessionConfigEntity create(@RequestBody CreateSessionRequest request) {
        return sessionManagementService.createSession(
                request.getSessionId(), 
                request.getUserId(), 
                request.getAgentId()
        );
    }
    
    @PutMapping("/{sessionId}/status")
    public SessionConfigEntity updateStatus(@PathVariable String sessionId, @RequestParam String status) {
        return sessionManagementService.updateSessionStatus(sessionId, status);
    }
    
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(@PathVariable String sessionId) {
        sessionManagementService.deleteSession(sessionId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/clean-expired")
    public ResponseEntity<Integer> cleanExpired() {
        int count = sessionManagementService.cleanExpiredSessions();
        return ResponseEntity.ok(count);
    }
    
    @lombok.Data
    public static class CreateSessionRequest {
        private String sessionId;
        private String userId;
        private String agentId;
    }
}