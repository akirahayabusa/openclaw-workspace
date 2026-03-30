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
package io.luoshen.quality.config;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.subagent.SubAgentProvider;
import io.luoshen.quality.tools.QualityTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quality Agent 配置类
 */
@Configuration
public class QualityAgentConfig {
    
    private static final String QUALITY_SYSTEM_PROMPT = """
        你是洛神系统的质量智能体（Quality Agent），负责质量管理相关工作。
        
        ## 你的职责
        
        1. **质量检测**：执行产品质量检测，记录检测结果
        2. **问题分析**：分析质量问题原因，提供改进建议
        3. **质量报告**：生成质量统计报告和趋势分析
        4. **标准管理**：维护质量标准和检测规范
        
        ## 工作原则
        
        - 检测要全面，不遗漏关键指标
        - 问题分析要追根溯源
        - 报告要数据驱动，有理有据
        - 改进建议要具体可行
        """;
    
    @Autowired
    private Model luoshenModel;
    
    @Bean
    public Toolkit qualityToolkit() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new QualityTools());
        return toolkit;
    }
    
    @Bean("qualityAgent")
    public ReActAgent qualityAgent(Toolkit qualityToolkit) {
        return ReActAgent.builder()
                .name("quality-agent")
                .description("洛神系统质量智能体，负责质量管理任务")
                .sysPrompt(QUALITY_SYSTEM_PROMPT)
                .model(luoshenModel)
                .toolkit(qualityToolkit)
                .memory(new InMemoryMemory())
                .build();
    }
    
    @Bean("qualityAgentProvider")
    public SubAgentProvider<ReActAgent> qualityAgentProvider() {
        return () -> ReActAgent.builder()
                .name("quality-agent")
                .description("质量智能体，处理质量相关任务")
                .sysPrompt(QUALITY_SYSTEM_PROMPT)
                .model(luoshenModel)
                .toolkit(qualityToolkit())
                .memory(new InMemoryMemory())
                .build();
    }
}