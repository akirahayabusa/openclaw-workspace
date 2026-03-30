/*
 * Copyright 2024-2026 Luoshen Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.luoshen.core.config;

import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.memory.autocontext.AutoContextConfig;
import io.agentscope.core.memory.autocontext.AutoContextMemory;
import io.agentscope.core.model.Model;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Memory 配置类
 * 
 * 提供短期记忆和自动上下文压缩能力
 */
@Configuration
public class LuoshenMemoryConfig {
    
    @Value("${luoshen.memory.type:inmemory}")
    private String memoryType;
    
    @Value("${luoshen.memory.auto-context.msg-threshold:30}")
    private int msgThreshold;
    
    @Value("${luoshen.memory.auto-context.last-keep:10}")
    private int lastKeep;
    
    @Value("${luoshen.memory.auto-context.token-ratio:0.3}")
    private double tokenRatio;
    
    /**
     * 创建默认的短期记忆实例
     * 
     * 使用 InMemoryMemory 作为基础实现
     */
    @Bean
    public Memory luoshenMemory() {
        return new InMemoryMemory();
    }
    
    /**
     * 创建自动上下文压缩记忆实例
     * 
     * 需要配合 Model 使用，用于智能压缩对话历史
     */
    @Bean
    public AutoContextMemory luoshenAutoContextMemory(Model luoshenModel) {
        AutoContextConfig config = AutoContextConfig.builder()
                .msgThreshold(msgThreshold)
                .lastKeep(lastKeep)
                .tokenRatio(tokenRatio)
                .build();
        return new AutoContextMemory(config, luoshenModel);
    }
}