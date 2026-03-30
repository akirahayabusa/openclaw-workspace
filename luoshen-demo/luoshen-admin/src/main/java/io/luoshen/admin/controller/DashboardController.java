/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.AgentConfigEntity;
import io.luoshen.admin.repository.AgentConfigRepository;
import io.luoshen.admin.repository.SkillConfigRepository;
import io.luoshen.admin.repository.SessionConfigRepository;
import io.luoshen.admin.repository.MemoryConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard 统计 API
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    
    private final AgentConfigRepository agentConfigRepository;
    private final SkillConfigRepository skillConfigRepository;
    private final SessionConfigRepository sessionConfigRepository;
    private final MemoryConfigRepository memoryConfigRepository;
    
    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Agent 统计
        long totalAgents = agentConfigRepository.count();
        long enabledAgents = agentConfigRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getEnabled()))
                .count();
        
        stats.put("totalAgents", totalAgents);
        stats.put("enabledAgents", enabledAgents);
        stats.put("disabledAgents", totalAgents - enabledAgents);
        
        // Skill 统计
        stats.put("totalSkills", skillConfigRepository.count());
        
        // Session 统计
        stats.put("activeSessions", sessionConfigRepository.count());
        
        // Memory 统计
        stats.put("totalMemories", memoryConfigRepository.count());
        
        // 系统状态
        stats.put("status", "running");
        stats.put("timestamp", System.currentTimeMillis());
        
        return stats;
    }
    
    /**
     * 获取最近活动
     */
    @GetMapping("/recent")
    public Map<String, Object> getRecentActivity() {
        Map<String, Object> activity = new HashMap<>();
        
        // 最近 Agent
        List<AgentConfigEntity> recentAgents = agentConfigRepository.findTop5ByOrderByCreatedAtDesc();
        activity.put("recentAgents", recentAgents);
        
        return activity;
    }
}
