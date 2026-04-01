/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.skill.AgentSkill;
import io.agentscope.skill.FileSkillRepository;
import io.agentscope.skill.SkillConfig;
import io.luoshen.admin.model.AgentSkillRelationEntity;
import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.AgentSkillRelationRepository;
import io.luoshen.admin.repository.SkillConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent 技能加载器
 * <p>
 * 负责为 Agent 加载技能，支持三种绑定模式：
 * - inherit: 继承父级技能
 * - custom: 自定义技能列表
 * - none: 禁用所有技能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentSkillLoader {

    private final SkillConfigRepository skillConfigRepository;
    private final AgentSkillRelationRepository relationRepository;
    private final ObjectMapper objectMapper;

    /**
     * 为 Agent 加载技能列表
     *
     * @param agentId Agent ID
     * @return 技能列表
     */
    public List<AgentSkill> loadSkillsForAgent(String agentId) {
        log.debug("开始为 Agent {} 加载技能", agentId);

        // 1. 查询 Agent 的技能绑定关系
        List<AgentSkillRelationEntity> relations = relationRepository.findByAgentIdAndEnabled(agentId, true);

        if (relations.isEmpty()) {
            log.debug("Agent {} 没有绑定任何技能", agentId);
            return List.of();
        }

        // 2. 按 bindMode 分组
        Map<String, List<AgentSkillRelationEntity>> grouped = relations.stream()
            .collect(Collectors.groupingBy(AgentSkillRelationEntity::getBindMode));

        List<AgentSkill> skills = new ArrayList<>();

        // 3. 处理 inherit 模式
        if (grouped.containsKey("inherit")) {
            List<AgentSkill> inheritedSkills = loadInheritSkills(agentId);
            skills.addAll(inheritedSkills);
        }

        // 4. 处理 custom 模式
        if (grouped.containsKey("custom")) {
            List<AgentSkillRelationEntity> customRelations = grouped.get("custom");
            List<AgentSkill> customSkills = loadCustomSkills(customRelations);
            skills.addAll(customSkills);
        }

        // 5. 按 priority 排序
        skills.sort(Comparator.comparingInt(skill -> {
            // 从 relation 中获取 priority
            Optional<AgentSkillRelationEntity> relation = relations.stream()
                .filter(r -> r.getSkillId().equals(skill.getSkillConfig().getName()))
                .findFirst();
            return relation.map(AgentSkillRelationEntity::getPriority).orElse(0);
        }));

        log.info("Agent {} 加载了 {} 个技能", agentId, skills.size());
        return skills;
    }

    /**
     * 加载继承模式的技能
     * <p>
     * 暂时简化为：加载所有启用的技能
     * TODO: 实现真正的层级继承（递归获取父级技能）
     *
     * @param agentId Agent ID
     * @return 技能列表
     */
    private List<AgentSkill> loadInheritSkills(String agentId) {
        log.debug("加载继承模式的技能: {}", agentId);

        // 简化实现：返回所有启用的技能
        List<SkillConfigEntity> enabledSkills = skillConfigRepository.findByEnabled(true);

        return enabledSkills.stream()
            .map(this::loadSkillFromConfig)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());
    }

    /**
     * 加载自定义模式的技能
     *
     * @param relations 绑定关系列表
     * @return 技能列表
     */
    private List<AgentSkill> loadCustomSkills(List<AgentSkillRelationEntity> relations) {
        log.debug("加载自定义模式的技能，共 {} 个", relations.size());

        List<AgentSkill> skills = new ArrayList<>();

        for (AgentSkillRelationEntity relation : relations) {
            String skillId = relation.getSkillId();

            Optional<SkillConfigEntity> skillConfig = skillConfigRepository.findBySkillId(skillId);
            if (skillConfig.isEmpty()) {
                log.warn("技能不存在: {}", skillId);
                continue;
            }

            if (!skillConfig.get().getEnabled()) {
                log.debug("技能已禁用: {}", skillId);
                continue;
            }

            Optional<AgentSkill> skill = loadSkillFromConfig(skillConfig.get());
            if (skill.isPresent()) {
                skills.add(skill.get());
            }
        }

        return skills;
    }

    /**
     * 从技能配置加载技能定义
     *
     * @param config 技能配置
     * @return 技能定义
     */
    private Optional<AgentSkill> loadSkillFromConfig(SkillConfigEntity config) {
        try {
            if (config.getPackagePath() != null && !config.getPackagePath().isEmpty()) {
                // 从文件系统加载
                return loadSkillFromFileSystem(config);
            } else {
                // 从内容加载
                return loadSkillFromContent(config);
            }
        } catch (Exception e) {
            log.error("加载技能失败: {}", config.getSkillId(), e);
            return Optional.empty();
        }
    }

    /**
     * 从文件系统加载技能
     *
     * @param config 技能配置
     * @return 技能定义
     */
    private Optional<AgentSkill> loadSkillFromFileSystem(SkillConfigEntity config) {
        try {
            Path skillPath = Paths.get(config.getPackagePath());

            if (!java.nio.file.Files.exists(skillPath)) {
                log.warn("技能包路径不存在: {}", skillPath);
                return Optional.empty();
            }

            // 使用 AgentScope 的 FileSkillRepository 加载
            FileSkillRepository repository = new FileSkillRepository(skillPath, false);
            return Optional.ofNullable(repository.getSkill(config.getSkillId()));

        } catch (Exception e) {
            log.error("从文件系统加载技能失败: {}", config.getSkillId(), e);
            return Optional.empty();
        }
    }

    /**
     * 从内容加载技能（纯文本技能）
     *
     * @param config 技能配置
     * @return 技能定义
     */
    private Optional<AgentSkill> loadSkillFromContent(SkillConfigEntity config) {
        try {
            if (config.getContent() == null || config.getContent().isEmpty()) {
                log.warn("技能内容为空: {}", config.getSkillId());
                return Optional.empty();
            }

            // 创建 SkillConfig
            SkillConfig skillConfig = new SkillConfig();
            skillConfig.setName(config.getSkillId());
            skillConfig.setDescription(config.getDescription());

            // 创建 AgentSkill
            AgentSkill skill = new AgentSkill(skillConfig);
            skill.setSkill(config.getContent());

            return Optional.of(skill);

        } catch (Exception e) {
            log.error("从内容加载技能失败: {}", config.getSkillId(), e);
            return Optional.empty();
        }
    }

    /**
     * 获取 Agent 的技能配置摘要
     *
     * @param agentId Agent ID
     * @return 配置摘要
     */
    public Map<String, Object> getAgentSkillSummary(String agentId) {
        List<AgentSkillRelationEntity> relations = relationRepository.findByAgentId(agentId);

        Map<String, Object> summary = new HashMap<>();
        summary.put("agentId", agentId);
        summary.put("totalSkills", relations.size());
        summary.put("enabledSkills", relations.stream().filter(AgentSkillRelationEntity::getEnabled).count());

        // 按模式分组统计
        Map<String, Long> modeStats = relations.stream()
            .collect(Collectors.groupingBy(AgentSkillRelationEntity::getBindMode, Collectors.counting()));
        summary.put("modeStats", modeStats);

        return summary;
    }
}
