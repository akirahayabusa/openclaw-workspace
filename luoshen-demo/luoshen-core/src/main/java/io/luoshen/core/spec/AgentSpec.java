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
package io.luoshen.core.spec;

import java.util.List;

/**
 * Agent 规格定义
 * 
 * 用于从 Markdown 文件或配置中定义 Agent 的属性
 * 包含名称、描述、系统提示和可用工具列表
 */
public record AgentSpec(
        /**
         * Agent 唯一标识符
         */
        String name,
        
        /**
         * Agent 描述，说明何时使用该 Agent
         */
        String description,
        
        /**
         * 系统提示内容
         */
        String systemPrompt,
        
        /**
         * 可用工具名称列表
         */
        List<String> toolNames,
        
        /**
         * 可用技能名称列表
         */
        List<String> skillNames
) {
    
    /**
     * 创建最小规格
     */
    public static AgentSpec of(String name, String description, String systemPrompt) {
        return new AgentSpec(name, description, systemPrompt, List.of(), List.of());
    }
    
    /**
     * 创建带工具的规格
     */
    public static AgentSpec of(String name, String description, String systemPrompt, 
                               List<String> toolNames) {
        return new AgentSpec(name, description, systemPrompt, toolNames, List.of());
    }
    
    /**
     * 创建完整规格
     */
    public static AgentSpec of(String name, String description, String systemPrompt,
                               List<String> toolNames, List<String> skillNames) {
        return new AgentSpec(name, description, systemPrompt, toolNames, skillNames);
    }
}