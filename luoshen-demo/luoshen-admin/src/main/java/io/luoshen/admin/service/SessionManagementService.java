/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import io.luoshen.admin.model.SessionConfigEntity;
import io.luoshen.admin.repository.SessionConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Session 管理服务
 * 
 * 核心功能：
 * 1. Session 创建、查询、删除
 * 2. Session 状态管理
 * 3. Session 过期清理
 */
@Slf4j
@Service
public class SessionManagementService {
    
    private final SessionConfigRepository sessionConfigRepository;
    
    public SessionManagementService(SessionConfigRepository sessionConfigRepository) {
        this.sessionConfigRepository = sessionConfigRepository;
    }
    
    /**
     * 创建 Session
     */
    @Transactional
    public SessionConfigEntity createSession(String sessionId, String userId, String agentId) {
        if (sessionConfigRepository.findBySessionId(sessionId).isPresent()) {
            throw new IllegalArgumentException("Session ID 已存在: " + sessionId);
        }
        
        SessionConfigEntity session = new SessionConfigEntity();
        session.setSessionId(sessionId);
        session.setUserId(userId);
        session.setAgentId(agentId);
        session.setStatus("active");
        session.setMessageCount(0);
        
        return sessionConfigRepository.save(session);
    }
    
    /**
     * 获取 Session
     */
    public SessionConfigEntity getSession(String sessionId) {
        return sessionConfigRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session 不存在: " + sessionId));
    }
    
    /**
     * 获取用户的所有 Session
     */
    public List<SessionConfigEntity> getUserSessions(String userId) {
        return sessionConfigRepository.findByUserId(userId);
    }
    
    /**
     * 获取 Agent 的所有 Session
     */
    public List<SessionConfigEntity> getAgentSessions(String agentId) {
        return sessionConfigRepository.findByAgentId(agentId);
    }
    
    /**
     * 更新 Session 状态
     */
    @Transactional
    public SessionConfigEntity updateSessionStatus(String sessionId, String status) {
        SessionConfigEntity session = getSession(sessionId);
        session.setStatus(status);
        return sessionConfigRepository.save(session);
    }
    
    /**
     * 更新 Session 数据
     */
    @Transactional
    public SessionConfigEntity updateSessionData(String sessionId, String sessionData, int messageCount) {
        SessionConfigEntity session = getSession(sessionId);
        session.setSessionData(sessionData);
        session.setMessageCount(messageCount);
        return sessionConfigRepository.save(session);
    }
    
    /**
     * 删除 Session
     */
    @Transactional
    public void deleteSession(String sessionId) {
        sessionConfigRepository.deleteBySessionId(sessionId);
        log.info("删除 Session: {}", sessionId);
    }
    
    /**
     * 清理过期 Session
     */
    @Transactional
    public int cleanExpiredSessions() {
        List<SessionConfigEntity> expired = sessionConfigRepository.findAll().stream()
                .filter(s -> s.getExpiresAt() != null && s.getExpiresAt().isBefore(LocalDateTime.now()))
                .toList();
        
        expired.forEach(s -> sessionConfigRepository.deleteBySessionId(s.getSessionId()));
        log.info("清理过期 Session: {} 个", expired.size());
        
        return expired.size();
    }
    
    /**
     * 获取所有 Session
     */
    public List<SessionConfigEntity> listAllSessions() {
        return sessionConfigRepository.findAll();
    }
    
    /**
     * 获取活跃 Session 数量
     */
    public long getActiveSessionCount() {
        return sessionConfigRepository.findByStatus("active").size();
    }
}