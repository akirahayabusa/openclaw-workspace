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
package io.luoshen.device.config;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.subagent.SubAgentProvider;
import io.luoshen.device.tools.DeviceTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Device Agent 配置类
 * 
 * 创建设备智能体，配置设备管理工具
 */
@Configuration
public class DeviceAgentConfig {
    
    private static final String DEVICE_SYSTEM_PROMPT = """
        你是洛神系统的设备智能体（Device Agent），负责管理和操作设备。
        
        ## 你的职责
        
        1. **设备状态查询**：查询设备的运行状态、参数、历史记录
        2. **设备控制**：执行设备控制命令，如启动、停止、调整参数
        3. **故障诊断**：分析设备故障原因，提供解决方案
        4. **设备报告**：生成设备运行报告和维护建议
        
        ## 工作原则
        
        - 操作设备前先确认设备状态
        - 控制命令需要明确参数
        - 故障诊断要系统化分析
        - 报告要简洁清晰，突出关键指标
        """;
    
    @Autowired
    private Model luoshenModel;
    
    /**
     * 创建 Device Agent 的 Toolkit
     */
    @Bean
    public Toolkit deviceToolkit() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new DeviceTools());
        return toolkit;
    }
    
    /**
     * 创建 Device Agent
     */
    @Bean("deviceAgent")
    public ReActAgent deviceAgent(Toolkit deviceToolkit) {
        return ReActAgent.builder()
                .name("device-agent")
                .description("洛神系统设备智能体，负责设备管理任务")
                .sysPrompt(DEVICE_SYSTEM_PROMPT)
                .model(luoshenModel)
                .toolkit(deviceToolkit)
                .memory(new InMemoryMemory())
                .build();
    }
    
    /**
     * 创建 Device Agent Provider
     * 
     * 用于 Leader Agent 通过 subAgent 工具调用
     */
    @Bean("deviceAgentProvider")
    public SubAgentProvider<ReActAgent> deviceAgentProvider() {
        return () -> ReActAgent.builder()
                .name("device-agent")
                .description("设备智能体，处理设备相关任务")
                .sysPrompt(DEVICE_SYSTEM_PROMPT)
                .model(luoshenModel)
                .toolkit(deviceToolkit())
                .memory(new InMemoryMemory())
                .build();
    }
}