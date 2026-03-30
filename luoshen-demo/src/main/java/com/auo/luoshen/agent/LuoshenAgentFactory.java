package com.auo.luoshen.agent;

import com.auo.luoshen.config.LuoshenConfig;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.tool.Toolkit;
import org.springframework.stereotype.Component;

/**
 * LuoshenAgentFactory - 洛神系统智能体工厂
 *
 * 负责动态创建和配置各级智能体：
 * - Leader/Master Agent
 * - Core Agent (设备Agent)
 * - Sub Agent (质量Agent, 物料Agent)
 */
@Component
public class LuoshenAgentFactory {

    private final LuoshenConfig config;

    public LuoshenAgentFactory(LuoshenConfig config) {
        this.config = config;
    }

    /**
     * 创建 Leader/Master Agent (总控智能体)
     */
    public ReActAgent createLeaderAgent() {
        return ReActAgent.builder()
                .name("LeaderAgent")
                .sysPrompt("""
                        你是洛神系统的 Leader/Master Agent，负责总体协调和任务分发。

                        你的职责：
                        1. 理解用户的整体需求
                        2. 将任务分解给合适的子智能体
                        3. 汇总和整合各智能体的返回结果
                        4. 给用户最终的回答

                        可用的子智能体：
                        - DeviceAgent (设备智能体): 负责设备相关的操作和查询
                        - QualityAgent (质量智能体): 负责质量相关的检查和分析
                        - MaterialAgent (物料智能体): 负责物料相关的管理和查询

                        当用户的问题涉及多个领域时，你需要协调多个子智能体共同完成任务。
                        """)
                .model(createDashScopeModel())
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();
    }

    /**
     * 创建 Core Agent (设备智能体)
     */
    public ReActAgent createDeviceAgent() {
        return ReActAgent.builder()
                .name("DeviceAgent")
                .sysPrompt("""
                        你是洛神系统的 DeviceAgent (设备智能体)，负责所有设备相关的操作。

                        你的职责：
                        1. 设备状态查询和监控
                        2. 设备操作控制（启动、停止、维护等）
                        3. 设备故障诊断和报告
                        4. 协调下面的子智能体完成具体任务

                        你可以调用的子智能体：
                        - QualityAgent (质量智能体): 协助处理质量相关的设备数据
                        - MaterialAgent (物料智能体): 协助处理设备相关的物料信息
                        """)
                .model(createDashScopeModel())
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();
    }

    /**
     * 创建 Sub Agent (质量智能体)
     */
    public ReActAgent createQualityAgent() {
        return ReActAgent.builder()
                .name("QualityAgent")
                .sysPrompt("""
                        你是洛神系统的 QualityAgent (质量智能体)，专门负责质量管理和分析。

                        你的职责：
                        1. 产品质量检测和分析
                        2. 质量指标监控和报告
                        3. 质量问题诊断和改进建议
                        4. 质量数据统计和可视化
                        """)
                .model(createDashScopeModel())
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();
    }

    /**
     * 创建 Sub Agent (物料智能体)
     */
    public ReActAgent createMaterialAgent() {
        return ReActAgent.builder()
                .name("MaterialAgent")
                .sysPrompt("""
                        你是洛神系统的 MaterialAgent (物料智能体)，专门负责物料管理。

                        你的职责：
                        1. 物料库存查询和管理
                        2. 物料采购需求分析
                        3. 物料使用情况追踪
                        4. 物料供应链协调
                        """)
                .model(createDashScopeModel())
                .memory(new InMemoryMemory())
                .toolkit(new Toolkit())
                .build();
    }

    /**
     * 创建 DashScope 模型
     */
    private DashScopeChatModel createDashScopeModel() {
        return DashScopeChatModel.builder()
                .apiKey(config.getApiKey())
                .modelName(config.getModelName())
                .stream(true)
                .enableThinking(false)
                .formatter(new DashScopeChatFormatter())
                .build();
    }

}
