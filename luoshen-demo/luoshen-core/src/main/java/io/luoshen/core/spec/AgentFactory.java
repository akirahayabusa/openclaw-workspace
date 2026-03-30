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

import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.InMemoryMemory;
import io.agentscope.core.model.Model;
import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.SkillBox;
import io.agentscope.core.tool.Toolkit;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * Agent 工厂类
 * 
 * 根据 AgentSpec 创建 ReActAgent 实例
 * 支持工具过滤和技能绑定
 */
public class AgentFactory {
    
    private final Model model;
    private final Map<String, Object> defaultToolsByName;
    private final Map<String, AgentSkill> defaultSkillsByName;
    
    public AgentFactory(Model model, 
                        Map<String, Object> defaultToolsByName,
                        Map<String, AgentSkill> defaultSkillsByName) {
        Assert.notNull(model, "model 不能为空");
        this.model = model;
        this.defaultToolsByName = defaultToolsByName != null ? Map.copyOf(defaultToolsByName) : Map.of();
        this.defaultSkillsByName = defaultSkillsByName != null ? Map.copyOf(defaultSkillsByName) : Map.of();
    }
    
    /**
     * 根据 AgentSpec 创建 ReActAgent
     * 
     * @param spec Agent 规格
     * @return ReActAgent 实例
     */
    public ReActAgent create(AgentSpec spec) {
        Assert.notNull(spec, "spec 不能为空");
        
        // 创建 Toolkit 并注册工具
        Toolkit toolkit = new Toolkit();
        registerTools(toolkit, spec.toolNames());
        
        // 创建 SkillBox 并注册技能
        SkillBox skillBox = new SkillBox(toolkit);
        registerSkills(skillBox, spec.skillNames());
        
        // 创建 Agent
        return ReActAgent.builder()
                .name(spec.name())
                .description(spec.description())
                .sysPrompt(spec.systemPrompt() != null ? spec.systemPrompt() : "")
                .model(model)
                .toolkit(toolkit)
                .skillBox(skillBox)
                .memory(new InMemoryMemory())
                .build();
    }
    
    /**
     * 注册工具
     * 
     * 如果 toolNames 为空，注册所有默认工具
     * 否则只注册指定的工具
     */
    private void registerTools(Toolkit toolkit, List<String> toolNames) {
        if (CollectionUtils.isEmpty(toolNames)) {
            defaultToolsByName.values().forEach(toolkit::registerTool);
        } else {
            for (String name : toolNames) {
                Object tool = defaultToolsByName.get(name);
                if (tool != null) {
                    toolkit.registerTool(tool);
                }
            }
        }
    }
    
    /**
     * 注册技能
     */
    private void registerSkills(SkillBox skillBox, List<String> skillNames) {
        if (CollectionUtils.isEmpty(skillNames)) {
            return;
        }
        for (String name : skillNames) {
            AgentSkill skill = defaultSkillsByName.get(name);
            if (skill != null) {
                skillBox.registration().skill(skill).apply();
            }
        }
    }
    
    /**
     * 创建 Builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private Model model;
        private Map<String, Object> defaultToolsByName = Map.of();
        private Map<String, AgentSkill> defaultSkillsByName = Map.of();
        
        public Builder model(Model model) {
            this.model = model;
            return this;
        }
        
        public Builder defaultToolsByName(Map<String, Object> defaultToolsByName) {
            this.defaultToolsByName = defaultToolsByName != null ? defaultToolsByName : Map.of();
            return this;
        }
        
        public Builder defaultSkillsByName(Map<String, AgentSkill> defaultSkillsByName) {
            this.defaultSkillsByName = defaultSkillsByName != null ? defaultSkillsByName : Map.of();
            return this;
        }
        
        public AgentFactory build() {
            Assert.notNull(model, "model 必须提供");
            return new AgentFactory(model, defaultToolsByName, defaultSkillsByName);
        }
    }
}