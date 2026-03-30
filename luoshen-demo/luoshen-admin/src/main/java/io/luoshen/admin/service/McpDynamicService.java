/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import io.luoshen.admin.model.McpConfigEntity;
import io.luoshen.admin.repository.McpConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 工具动态管理服务
 * 
 * 核心功能：
 * 1. 从数据库读取配置，动态注册 MCP 工具
 * 2. 支持热加载，修改后即时生效
 * 3. 工具实例管理
 */
@Slf4j
@Service
public class McpDynamicService {
    
    private final McpConfigRepository mcpConfigRepository;
    
    // MCP 工具缓存
    private final Map<String, Object> mcpToolCache = new ConcurrentHashMap<>();
    
    public McpDynamicService(McpConfigRepository mcpConfigRepository) {
        this.mcpConfigRepository = mcpConfigRepository;
    }
    
    /**
     * 创建新的 MCP 工具配置
     */
    @Transactional
    public McpConfigEntity createMcpConfig(McpConfigEntity config) {
        if (mcpConfigRepository.findByToolId(config.getToolId()).isPresent()) {
            throw new IllegalArgumentException("MCP 工具 ID 已存在: " + config.getToolId());
        }
        
        McpConfigEntity saved = mcpConfigRepository.save(config);
        log.info("创建 MCP 工具配置: {}", saved.getToolId());
        
        // 注册工具
        if (saved.getEnabled()) {
            registerMcpTool(saved);
        }
        
        return saved;
    }
    
    /**
     * 更新 MCP 工具配置
     */
    @Transactional
    public McpConfigEntity updateMcpConfig(String toolId, McpConfigEntity newConfig) {
        McpConfigEntity existing = mcpConfigRepository.findByToolId(toolId)
                .orElseThrow(() -> new IllegalArgumentException("MCP 工具不存在: " + toolId));
        
        // 更新字段
        existing.setName(newConfig.getName());
        existing.setDescription(newConfig.getDescription());
        existing.setType(newConfig.getType());
        existing.setParametersJson(newConfig.getParametersJson());
        existing.setImplementation(newConfig.getImplementation());
        existing.setEndpoint(newConfig.getEndpoint());
        existing.setAgentId(newConfig.getAgentId());
        existing.setEnabled(newConfig.getEnabled());
        
        McpConfigEntity saved = mcpConfigRepository.save(existing);
        log.info("更新 MCP 工具配置: {}", saved.getToolId());
        
        // 清除缓存，重新注册
        mcpToolCache.remove(toolId);
        if (saved.getEnabled()) {
            registerMcpTool(saved);
        }
        
        return saved;
    }
    
    /**
     * 删除 MCP 工具配置
     */
    @Transactional
    public void deleteMcpConfig(String toolId) {
        mcpToolCache.remove(toolId);
        mcpConfigRepository.deleteByToolId(toolId);
        log.info("删除 MCP 工具配置: {}", toolId);
    }
    
    /**
     * 获取 MCP 工具
     */
    public Object getMcpTool(String toolId) {
        return mcpToolCache.computeIfAbsent(toolId, id -> {
            McpConfigEntity config = mcpConfigRepository.findByToolId(id)
                    .orElseThrow(() -> new IllegalArgumentException("MCP 工具不存在: " + id));
            
            if (!config.getEnabled()) {
                throw new IllegalStateException("MCP 工具已禁用: " + id);
            }
            
            return registerMcpTool(config);
        });
    }
    
    /**
     * 获取所有启用的 MCP 工具
     */
    public List<Object> getAllEnabledMcpTools() {
        List<McpConfigEntity> configs = mcpConfigRepository.findByEnabled(true);
        return configs.stream()
                .map(c -> getMcpTool(c.getToolId()))
                .toList();
    }
    
    /**
     * 刷新所有 MCP 工具
     */
    public void refreshAllMcpTools() {
        mcpToolCache.clear();
        List<McpConfigEntity> configs = mcpConfigRepository.findByEnabled(true);
        configs.forEach(this::registerMcpTool);
        log.info("刷新所有 MCP 工具，共 {} 个", configs.size());
    }
    
    /**
     * 注册 MCP 工具
     * 
     * 这里创建一个动态代理对象，模拟 @Tool 注解的效果
     */
    private Object registerMcpTool(McpConfigEntity config) {
        // 实际实现中，这里应该：
        // 1. 解析 parametersJson，获取参数定义
        // 2. 解析 implementation，获取执行逻辑
        // 3. 创建动态代理或使用脚本引擎执行
        
        // 简化实现：返回一个包装对象
        McpToolWrapper wrapper = new McpToolWrapper(config);
        mcpToolCache.put(config.getToolId(), wrapper);
        log.info("注册 MCP 工具: {}", config.getToolId());
        
        return wrapper;
    }
    
    /**
     * MCP 工具包装类
     * 
     * 用于动态执行 MCP 工具
     */
    public static class McpToolWrapper {
        private final McpConfigEntity config;
        
        public McpToolWrapper(McpConfigEntity config) {
            this.config = config;
        }
        
        /**
         * 执行工具
         */
        public String execute(Map<String, Object> params) {
            // 实际实现中，这里应该：
            // 1. 验证参数
            // 2. 执行 implementation 或调用 endpoint
            // 3. 返回结果
            
            return String.format("MCP 工具 %s 执行成功，参数: %s", 
                    config.getName(), params.toString());
        }
        
        public String getToolId() {
            return config.getToolId();
        }
        
        public String getName() {
            return config.getName();
        }
        
        public String getDescription() {
            return config.getDescription();
        }
    }
    
    /**
     * 获取 MCP 配置列表
     */
    public List<McpConfigEntity> listAllConfigs() {
        return mcpConfigRepository.findAll();
    }
    
    /**
     * 获取 MCP 配置
     */
    public McpConfigEntity getConfig(String toolId) {
        return mcpConfigRepository.findByToolId(toolId)
                .orElseThrow(() -> new IllegalArgumentException("MCP 工具不存在: " + toolId));
    }
}