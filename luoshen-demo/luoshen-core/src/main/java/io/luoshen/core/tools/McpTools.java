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
package io.luoshen.core.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;

/**
 * MCP 工具示例类
 * 
 * 展示如何使用 @Tool 和 @ToolParam 注解定义 MCP 工具
 * 这些工具可以被 Agent 动态调用
 */
public class McpTools {
    
    /**
     * MCP 工具示例：查询外部数据
     * 
     * 这个工具模拟从外部系统获取数据的能力
     * 实际使用时可以连接真实的 MCP 服务
     */
    @Tool(name = "mcp_query_external_data", description = "从外部系统查询数据")
    public String queryExternalData(
            @ToolParam(name = "source", description = "数据源名称，如 erp, mes, wms") String source,
            @ToolParam(name = "query_type", description = "查询类型: status, history, detail") String queryType,
            @ToolParam(name = "entity_id", description = "实体ID（可选）", required = false) String entityId
    ) {
        // 模拟返回数据
        return String.format("""
                外部数据查询结果
                ================
                数据源: %s
                查询类型: %s
                实体ID: %s
                
                模拟数据:
                - 状态: 正常
                - 最后更新: 2026-03-30 17:00:00
                - 数据来源: MCP 服务
                
                注意: 这是模拟数据，实际使用时需要配置真实的 MCP 服务端点。
                """,
                source, queryType, entityId != null ? entityId : "全部"
        );
    }
    
    /**
     * MCP 工具示例：调用外部服务
     */
    @Tool(name = "mcp_call_service", description = "调用外部服务执行操作")
    public String callService(
            @ToolParam(name = "service_name", description = "服务名称") String serviceName,
            @ToolParam(name = "action", description = "操作类型") String action,
            @ToolParam(name = "params", description = "操作参数（JSON 格式，可选）", required = false) String params
    ) {
        return String.format("""
                外部服务调用结果
                ================
                服务名称: %s
                操作类型: %s
                参数: %s
                
                执行状态: 成功
                返回时间: 2026-03-30 17:00:00
                
                注意: 这是模拟调用，实际使用时需要配置真实的 MCP 服务。
                """,
                serviceName, action, params != null ? params : "无"
        );
    }
    
    /**
     * MCP 工具示例：获取系统配置
     */
    @Tool(name = "mcp_get_config", description = "获取系统配置信息")
    public String getConfig(
            @ToolParam(name = "config_key", description = "配置键名（可选）", required = false) String configKey
    ) {
        if (configKey != null && !configKey.isEmpty()) {
            return String.format("""
                    配置查询结果
                    ============
                    配置键: %s
                    配置值: 示例配置值
                    
                    说明: 这是模拟配置数据。
                    """, configKey);
        }
        
        return """
                系统配置列表
                ============
                - system.name: 洛神系统
                - system.version: 1.0.0
                - system.mode: production
                
                说明: 这是模拟配置数据，实际使用时需要连接配置中心。
                """;
    }
}