/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.core.config;

import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.Memory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Memory 配置类
 */
@Configuration
public class LuoshenMemoryConfig {
    
    @Value("${luoshen.memory.type:inmemory}")
    private String memoryType;
    
    @Value("${luoshen.memory.max-size:100}")
    private int maxSize;
    
    /**
     * 创建默认的短期记忆实例
     */
    @Bean
    public Memory luoshenMemory() {
        return new InMemoryMemory();
    }
}