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

import io.agentscope.core.formatter.dashscope.DashScopeChatFormatter;
import io.agentscope.core.formatter.openai.OpenAIChatFormatter;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 模型配置类
 * 
 * 支持 DashScope (通义千问) 和 OpenAI 两种模型提供商
 * 通过配置参数动态选择模型类型
 */
@Configuration
public class LuoshenModelConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(LuoshenModelConfig.class);
    
    private static final String PROVIDER_DASHSCOPE = "dashscope";
    private static final String PROVIDER_OPENAI = "openai";
    
    @Value("${luoshen.model.provider:dashscope}")
    private String modelProvider;
    
    @Value("${luoshen.model.dashscope.api-key:}")
    private String dashscopeApiKey;
    
    @Value("${luoshen.model.dashscope.model-name:qwen-max}")
    private String dashscopeModelName;
    
    @Value("${luoshen.model.dashscope.base-url:}")
    private String dashscopeBaseUrl;
    
    @Value("${luoshen.model.openai.api-key:}")
    private String openaiApiKey;
    
    @Value("${luoshen.model.openai.model-name:gpt-4}")
    private String openaiModelName;
    
    @Value("${luoshen.model.openai.base-url:}")
    private String openaiBaseUrl;
    
    @Value("${luoshen.model.stream:true}")
    private boolean enableStream;
    
    @Value("${luoshen.model.enable-thinking:false}")
    private boolean enableThinking;
    
    /**
     * 创建共享的 Model 实例
     * 
     * 根据配置选择 DashScope 或 OpenAI 模型
     * 所有 Agent 共享同一个 Model 实例
     */
    @Bean
    public Model luoshenModel() {
        if (PROVIDER_OPENAI.equalsIgnoreCase(modelProvider)) {
            logger.info("创建 OpenAI 模型: model={}, baseUrl={}", openaiModelName, openaiBaseUrl);
            OpenAIChatModel.Builder builder = OpenAIChatModel.builder()
                    .apiKey(openaiApiKey)
                    .modelName(openaiModelName)
                    .stream(enableStream)
                    .formatter(new OpenAIChatFormatter());
            
            if (openaiBaseUrl != null && !openaiBaseUrl.isEmpty() && !openaiBaseUrl.equals("-")) {
                builder.baseUrl(openaiBaseUrl);
            }
            return builder.build();
        } else {
            logger.info("创建 DashScope 模型: model={}, baseUrl={}", dashscopeModelName, dashscopeBaseUrl);
            DashScopeChatModel.Builder builder = DashScopeChatModel.builder()
                    .apiKey(dashscopeApiKey)
                    .modelName(dashscopeModelName)
                    .stream(enableStream)
                    .enableThinking(enableThinking)
                    .formatter(new DashScopeChatFormatter());
            
            if (dashscopeBaseUrl != null && !dashscopeBaseUrl.isEmpty() && !dashscopeBaseUrl.equals("-")) {
                builder.baseUrl(dashscopeBaseUrl);
            }
            return builder.build();
        }
    }
}