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
package io.luoshen.material.config;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.core.tool.subagent.SubAgentProvider;
import io.luoshen.material.tools.MaterialTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Material Agent 配置类
 */
@Configuration
public class MaterialAgentConfig {
    
    private static final String MATERIAL_SYSTEM_PROMPT = """
        你是洛神系统的物料智能体（Material Agent），负责物料管理和库存相关工作。
        
        ## 你的职责
        
        1. **库存查询**：查询物料库存信息，监控库存状态
        2. **采购管理**：处理采购申请，跟踪采购状态
        3. **出入库管理**：记录物料出入库操作
        4. **报告生成**：生成物料管理报告
        
        ## 工作原则
        
        - 库存信息要准确及时
        - 采购申请要合理规范
        - 出入库记录要完整清晰
        - 报告要数据详实
        """;
    
    @Autowired
    private Model luoshenModel;
    
    @Bean
    public Toolkit materialToolkit() {
        Toolkit toolkit = new Toolkit();
        toolkit.registerTool(new MaterialTools());
        return toolkit;
    }
    
    @Bean("materialAgent")
    public ReActAgent materialAgent(Toolkit materialToolkit) {
        return ReActAgent.builder()
                .name("material-agent")
                .description("洛神系统物料智能体，负责物料管理任务")
                .sysPrompt(MATERIAL_SYSTEM_PROMPT)
                .model(luoshenModel)
                .toolkit(materialToolkit)
                .memory(new InMemoryMemory())
                .build();
    }
    
    @Bean("materialAgentProvider")
    public SubAgentProvider<ReActAgent> materialAgentProvider() {
        return () -> ReActAgent.builder()
                .name("material-agent")
                .description("物料智能体，处理物料相关任务")
                .sysPrompt(MATERIAL_SYSTEM_PROMPT)
                .model(luoshenModel)
                .toolkit(materialToolkit())
                .memory(new InMemoryMemory())
                .build();
    }
}