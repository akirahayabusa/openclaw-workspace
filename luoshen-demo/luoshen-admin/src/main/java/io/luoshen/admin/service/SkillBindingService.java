/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import io.luoshen.admin.model.AgentSkillRelationEntity;
import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.AgentSkillRelationRepository;
import io.luoshen.admin.repository.SkillConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 技能绑定服务
 * <p>
 * 负责管理 Agent 和 Skill 之间的绑定关系
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillBindingService {

    private final AgentSkillRelationRepository relationRepository;
    private final SkillConfigRepository skillConfigRepository;

    /**
     * 绑定技能到 Agent
     *
     * @param agentId Agent ID
     * @param skillId Skill ID
     * @param bindMode 绑定模式：inherit, custom, none
     * @param priority 优先级
     * @param config 配置参数（JSON 字符串）
     * @return 绑定关系
     */
    @Transactional
    public AgentSkillRelationEntity bindSkill(
        String agentId,
        String skillId,
        String bindMode,
        Integer priority,
        String config
    ) {
        // 1. 验证 Agent 和 Skill 是否存在
        if (!skillConfigRepository.existsBySkillId(skillId)) {
            throw new IllegalArgumentException("技能不存在: " + skillId);
        }

        // 2. 检查是否已经绑定
        if (relationRepository.existsByAgentIdAndSkillId(agentId, skillId)) {
            throw new IllegalArgumentException("技能已绑定: agent=" + agentId + ", skill=" + skillId);
        }

        // 3. 创建绑定关系
        AgentSkillRelationEntity relation = new AgentSkillRelationEntity();
        relation.setAgentId(agentId);
        relation.setSkillId(skillId);
        relation.setBindMode(bindMode != null ? bindMode : "custom");
        relation.setEnabled(true);
        relation.setPriority(priority != null ? priority : 0);
        relation.setConfig(config);

        AgentSkillRelationEntity saved = relationRepository.save(relation);
        log.info("绑定技能成功: agent={}, skill={}, mode={}", agentId, skillId, bindMode);

        return saved;
    }

    /**
     * 解绑 Agent 的技能
     *
     * @param agentId Agent ID
     * @param skillId Skill ID
     */
    @Transactional
    public void unbindSkill(String agentId, String skillId) {
        if (!relationRepository.existsByAgentIdAndSkillId(agentId, skillId)) {
            throw new IllegalArgumentException("绑定关系不存在: agent=" + agentId + ", skill=" + skillId);
        }

        relationRepository.deleteByAgentIdAndSkillId(agentId, skillId);
        log.info("解绑技能成功: agent={}, skill={}", agentId, skillId);
    }

    /**
     * 批量绑定技能
     *
     * @param agentId Agent ID
     * @param skillIds Skill ID 列表
     * @param bindMode 绑定模式
     * @return 绑定关系列表
     */
    @Transactional
    public List<AgentSkillRelationEntity> bindSkillsBatch(
        String agentId,
        List<String> skillIds,
        String bindMode
    ) {
        List<AgentSkillRelationEntity> results = new ArrayList<>();

        for (String skillId : skillIds) {
            try {
                // 检查技能是否存在
                if (!skillConfigRepository.existsBySkillId(skillId)) {
                    log.warn("技能不存在，跳过: {}", skillId);
                    continue;
                }

                // 检查是否已绑定
                if (relationRepository.existsByAgentIdAndSkillId(agentId, skillId)) {
                    log.warn("技能已绑定，跳过: agent={}, skill={}", agentId, skillId);
                    continue;
                }

                // 创建绑定
                AgentSkillRelationEntity relation = new AgentSkillRelationEntity();
                relation.setAgentId(agentId);
                relation.setSkillId(skillId);
                relation.setBindMode(bindMode != null ? bindMode : "custom");
                relation.setEnabled(true);
                relation.setPriority(0);

                results.add(relationRepository.save(relation));

            } catch (Exception e) {
                log.error("绑定技能失败: agent={}, skill={}", agentId, skillId, e);
            }
        }

        log.info("批量绑定完成: agent={}, 成功={}/{}", agentId, results.size(), skillIds.size());
        return results;
    }

    /**
     * 查询 Agent 的所有技能绑定关系
     *
     * @param agentId Agent ID
     * @return 绑定关系列表
     */
    public List<AgentSkillRelationEntity> getAgentSkillRelations(String agentId) {
        return relationRepository.findByAgentId(agentId);
    }

    /**
     * 查询 Agent 的已启用技能
     *
     * @param agentId Agent ID
     * @return 技能列表（包含技能详情）
     */
    public List<Map<String, Object>> getAgentSkills(String agentId) {
        List<AgentSkillRelationEntity> relations = relationRepository.findByAgentIdAndEnabled(agentId, true);

        return relations.stream()
            .map(relation -> {
                Map<String, Object> result = new HashMap<>();
                result.put("agentId", relation.getAgentId());
                result.put("skillId", relation.getSkillId());
                result.put("bindMode", relation.getBindMode());
                result.put("enabled", relation.getEnabled());
                result.put("priority", relation.getPriority());

                // 查询技能详情
                Optional<SkillConfigEntity> skillConfig = skillConfigRepository.findBySkillId(relation.getSkillId());
                skillConfig.ifPresent(config -> {
                    result.put("name", config.getName());
                    result.put("description", config.getDescription());
                    result.put("category", config.getCategory());
                    result.put("source", config.getSource());
                    result.put("riskLevel", config.getRiskLevel());
                });

                return result;
            })
            .collect(Collectors.toList());
    }

    /**
     * 设置 Agent 的技能配置（覆盖）
     *
     * @param agentId Agent ID
     * @param mode 绑定模式：inherit, custom, none
     * @param allowed 允许的技能列表（custom 模式使用）
     * @param denied 禁止的技能列表
     */
    @Transactional
    public void setAgentSkillConfig(
        String agentId,
        String mode,
        List<String> allowed,
        List<String> denied
    ) {
        // 1. 删除现有的所有绑定关系
        relationRepository.deleteByAgentId(agentId);

        // 2. 根据 mode 创建新的绑定关系
        if ("none".equals(mode)) {
            log.info("Agent {} 设置为无技能模式", agentId);
            return;
        }

        if ("inherit".equals(mode)) {
            // inherit 模式：为所有启用的技能创建绑定关系
            List<SkillConfigEntity> enabledSkills = skillConfigRepository.findByEnabled(true);
            for (SkillConfigEntity skill : enabledSkills) {
                // 检查是否在 denied 列表中
                if (denied != null && denied.contains(skill.getSkillId())) {
                    continue;
                }

                AgentSkillRelationEntity relation = new AgentSkillRelationEntity();
                relation.setAgentId(agentId);
                relation.setSkillId(skill.getSkillId());
                relation.setBindMode("inherit");
                relation.setEnabled(true);
                relation.setPriority(0);

                relationRepository.save(relation);
            }

            log.info("Agent {} 设置为继承模式，绑定 {} 个技能", agentId, enabledSkills.size());

        } else if ("custom".equals(mode)) {
            // custom 模式：只绑定 allowed 列表中的技能
            if (allowed != null && !allowed.isEmpty()) {
                for (String skillId : allowed) {
                    // 检查是否在 denied 列表中
                    if (denied != null && denied.contains(skillId)) {
                        continue;
                    }

                    // 检查技能是否存在
                    if (!skillConfigRepository.existsBySkillId(skillId)) {
                        log.warn("技能不存在，跳过: {}", skillId);
                        continue;
                    }

                    AgentSkillRelationEntity relation = new AgentSkillRelationEntity();
                    relation.setAgentId(agentId);
                    relation.setSkillId(skillId);
                    relation.setBindMode("custom");
                    relation.setEnabled(true);
                    relation.setPriority(0);

                    relationRepository.save(relation);
                }
            }

            log.info("Agent {} 设置为自定义模式，绑定 {} 个技能", agentId,
                allowed != null ? allowed.size() : 0);
        }
    }

    /**
     * 切换技能绑定状态
     *
     * @param agentId Agent ID
     * @param skillId Skill ID
     * @param enabled 是否启用
     */
    @Transactional
    public void toggleSkillBinding(String agentId, String skillId, boolean enabled) {
        AgentSkillRelationEntity relation = relationRepository.findByAgentIdAndSkillId(agentId, skillId)
            .orElseThrow(() -> new IllegalArgumentException("绑定关系不存在"));

        relation.setEnabled(enabled);
        relationRepository.save(relation);

        log.info("切换技能绑定状态: agent={}, skill={}, enabled={}", agentId, skillId, enabled);
    }

    /**
     * 删除 Agent 的所有技能绑定
     *
     * @param agentId Agent ID
     */
    @Transactional
    public void removeAllAgentSkills(String agentId) {
        long count = relationRepository.findByAgentId(agentId).size();
        relationRepository.deleteByAgentId(agentId);
        log.info("删除 Agent 的所有技能绑定: agent={}, count={}", agentId, count);
    }
}
