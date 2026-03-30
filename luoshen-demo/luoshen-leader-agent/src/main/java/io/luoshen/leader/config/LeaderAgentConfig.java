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
package io.luoshen.leader.config;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.subagent.SubAgentConfig;
import io.agentscope.core.tool.subagent.SubAgentProvider;
import io.luoshen.core.config.LuoshenSessionConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Leader Agent 配置类
 * 
 * 创建总控智能体，配置子智能体调用工具
 */
@Configuration
public class LeaderAgentConfig {
    
    private static final String LEADER_SYSTEM_PROMPT = """
        你是洛神系统的总控智能体（Leader Agent），负责协调和管理所有子智能体的工作。
        
        ## 你的职责
        
        1. **任务分析**：分析用户请求，判断需要哪些智能体参与
        2. **任务分发**：将任务分发给合适的子智能体
        3. **结果汇总**：收集各子智能体的结果，整合成完整的回复
        4. **状态管理**：跟踪任务执行状态，处理异常情况
        
        ## 可用的子智能体
        
        | 智能体 | 用途 | 调用时机 |
        |--------|------|----------|
        | device-agent | 设备管理 | 查询设备状态、控制设备、设备故障诊断 |
        | quality-agent | 质量管理 | 质量检测、质量问题分析、质量报告生成 |
        | material-agent | 物料管理 | 物料查询、库存管理、物料调度 |
        
        ## 工作流程
        
        1. 接收用户请求
        2. 分析请求类型和涉及的领域
        3. 决定需要调用哪些子智能体
        4. 使用 Task 工具分发给子智能体
        5. 收集结果并整合回复
        6. 向用户返回最终结果
        """;
    
    @Autowired
    private Model luoshenModel;
    
    @Autowired
    private LuoshenSessionConfig sessionConfig;
    
    // 子智能体 Provider（由各子智能体模块提供）
    @Autowired(required = false)
    @Qualifier("deviceAgentProvider")
    private SubAgentProvider<ReActAgent> deviceAgentProvider;
    
    @Autowired(required = false)
    @Qualifier("qualityAgentProvider")
    private SubAgentProvider<ReActAgent> qualityAgentProvider;
    
    @Autowired(required = false)
    @Qualifier("materialAgentProvider")
    private SubAgentProvider<ReActAgent> materialAgentProvider;
    
    /**
     * 创建 Leader Agent 的 Toolkit
     * 
     * 注册子智能体调用工具
     */
    @Bean
    public Toolkit leaderToolkit() {
        Toolkit toolkit = new Toolkit();
        
        // 注册子智能体工具
        if (deviceAgentProvider != null) {
            toolkit.registration()
                    .subAgent(deviceAgentProvider, SubAgentConfig.builder()
                            .toolName("call_device_agent")
                            .description("调用设备智能体处理设备相关任务")
                            .build())
                    .apply();
        }
        
        if (qualityAgentProvider != null) {
            toolkit.registration()
                    .subAgent(qualityAgentProvider, SubAgentConfig.builder()
                            .toolName("call_quality_agent")
                            .description("调用质量智能体处理质量相关任务")
                            .build())
                    .apply();
        }
        
        if (materialAgentProvider != null) {
            toolkit.registration()
                    .subAgent(materialAgentProvider, SubAgentConfig.builder()
                            .toolName("call_material_agent")
                            .description("调用物料智能体处理物料相关任务")
                            .build())
                    .apply();
        }
        
        return toolkit;
    }
    
    /**
     * 创建 Leader Agent
     */
    @Bean("leaderAgent")
    public ReActAgent leaderAgent(Toolkit leaderToolkit) {
        return ReActAgent.builder()
                .name("leader-agent")
                .description("洛神系统总控智能体，负责协调子智能体完成任务")
                .sysPrompt(LEADER_SYSTEM_PROMPT)
                .model(luoshenModel)
                .toolkit(leaderToolkit)
                .memory(new InMemoryMemory())
                .build();
    }
}