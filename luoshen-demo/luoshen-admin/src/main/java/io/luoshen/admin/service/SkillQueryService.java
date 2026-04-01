/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.SkillConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 技能查询和管理服务
 * <p>
 * 负责技能的创建、查询、更新、删除
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SkillQueryService {

    private final SkillConfigRepository skillConfigRepository;
    private final ObjectMapper objectMapper;

    /**
     * 创建纯文本技能（不上传文件）
     */
    @Transactional
    public SkillConfigEntity createTextSkill(CreateSkillRequest request) {
        // 1. 验证必填字段
        if (request.getSkillId() == null || request.getSkillId().isEmpty()) {
            throw new IllegalArgumentException("技能 ID 不能为空");
        }
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new IllegalArgumentException("技能名称不能为空");
        }
        if (request.getDescription() == null || request.getDescription().isEmpty()) {
            throw new IllegalArgumentException("技能描述不能为空");
        }
        if (request.getContent() == null || request.getContent().isEmpty()) {
            throw new IllegalArgumentException("技能内容不能为空");
        }

        // 2. 检查技能 ID 是否已存在
        if (skillConfigRepository.existsBySkillId(request.getSkillId())) {
            throw new IllegalArgumentException("技能 ID 已存在: " + request.getSkillId());
        }

        // 3. 创建技能实体
        SkillConfigEntity skill = new SkillConfigEntity();
        skill.setSkillId(request.getSkillId());
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        skill.setContent(request.getContent());
        skill.setSource("user_upload");
        skill.setPackagePath(null);  // 纯文本技能无包路径
        skill.setRiskLevel(request.getRiskLevel() != null ? request.getRiskLevel() : "low");
        skill.setFileSize(null);
        skill.setChecksum(null);
        skill.setHasScripts(false);
        skill.setScriptFiles(null);

        // 分类和标签
        skill.setCategory(request.getCategory());
        skill.setSubCategory(request.getSubCategory());
        skill.setTags(request.getTags());
        skill.setCustomType(request.getCustomType());

        // 能力特征（纯文本技能）
        try {
            Map<String, Boolean> capabilities = Map.of(
                "hasScripts", false,
                "hasAssets", false,
                "hasReferences", false,
                "needsNetwork", false
            );
            skill.setCapabilities(objectMapper.writeValueAsString(capabilities));
        } catch (JsonProcessingException e) {
            log.warn("序列化能力特征失败", e);
        }

        skill.setEnabled(true);

        // 4. 保存到数据库
        SkillConfigEntity savedSkill = skillConfigRepository.save(skill);
        
        log.info("创建纯文本技能成功: {}", savedSkill.getSkillId());
        return savedSkill;
    }

    /**
     * 查询技能详情
     */
    public Optional<SkillConfigEntity> getSkill(String skillId) {
        return skillConfigRepository.findBySkillId(skillId);
    }

    /**
     * 查询技能列表（分页）
     */
    public Page<SkillConfigEntity> listSkills(
        String category,
        String source,
        Boolean enabled,
        int page,
        int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (category != null && !category.isEmpty()) {
            if (enabled != null) {
                return skillConfigRepository.findByCategoryAndEnabled(category, enabled, pageable);
            }
            return skillConfigRepository.findByCategory(category, pageable);
        }
        
        if (source != null && !source.isEmpty()) {
            if (enabled != null) {
                return skillConfigRepository.findBySourceAndEnabled(source, enabled, pageable);
            }
            return skillConfigRepository.findBySource(source, pageable);
        }
        
        if (enabled != null) {
            return skillConfigRepository.findByEnabled(enabled, pageable);
        }
        
        return skillConfigRepository.findAll(pageable);
    }

    /**
     * 搜索技能（名称或描述包含关键词）
     */
    public List<SkillConfigEntity> searchSkills(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return List.of();
        }

        // 简单的模糊搜索（实际项目中建议使用全文搜索引擎）
        return skillConfigRepository.findAll().stream()
            .filter(skill -> 
                (skill.getName() != null && skill.getName().toLowerCase().contains(keyword.toLowerCase())) ||
                (skill.getDescription() != null && skill.getDescription().toLowerCase().contains(keyword.toLowerCase())) ||
                (skill.getSkillId() != null && skill.getSkillId().toLowerCase().contains(keyword.toLowerCase()))
            )
            .toList();
    }

    /**
     * 更新技能
     */
    @Transactional
    public SkillConfigEntity updateSkill(String skillId, UpdateSkillRequest request) {
        SkillConfigEntity skill = skillConfigRepository.findBySkillId(skillId)
            .orElseThrow(() -> new IllegalArgumentException("技能不存在: " + skillId));

        // 更新字段
        if (request.getName() != null && !request.getName().isEmpty()) {
            skill.setName(request.getName());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            skill.setDescription(request.getDescription());
        }
        if (request.getContent() != null && !request.getContent().isEmpty()) {
            skill.setContent(request.getContent());
        }
        if (request.getCategory() != null) {
            skill.setCategory(request.getCategory());
        }
        if (request.getSubCategory() != null) {
            skill.setSubCategory(request.getSubCategory());
        }
        if (request.getTags() != null) {
            skill.setTags(request.getTags());
        }
        if (request.getCustomType() != null) {
            skill.setCustomType(request.getCustomType());
        }
        if (request.getRiskLevel() != null) {
            skill.setRiskLevel(request.getRiskLevel());
        }
        if (request.getEnabled() != null) {
            skill.setEnabled(request.getEnabled());
        }

        SkillConfigEntity updatedSkill = skillConfigRepository.save(skill);
        log.info("更新技能成功: {}", updatedSkill.getSkillId());
        
        return updatedSkill;
    }

    /**
     * 删除技能
     */
    @Transactional
    public void deleteSkill(String skillId) {
        if (!skillConfigRepository.existsBySkillId(skillId)) {
            throw new IllegalArgumentException("技能不存在: " + skillId);
        }

        skillConfigRepository.deleteBySkillId(skillId);
        log.info("删除技能成功: {}", skillId);
    }

    /**
     * 切换技能启用状态
     */
    @Transactional
    public boolean toggleSkillEnabled(String skillId) {
        SkillConfigEntity skill = skillConfigRepository.findBySkillId(skillId)
            .orElseThrow(() -> new IllegalArgumentException("技能不存在: " + skillId));

        skill.setEnabled(!skill.getEnabled());
        skillConfigRepository.save(skill);
        
        log.info("切换技能状态: {} -> {}", skillId, skill.getEnabled());
        return skill.getEnabled();
    }

    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        return skillConfigRepository.findAll().stream()
            .map(SkillConfigEntity::getCategory)
            .filter(category -> category != null && !category.isEmpty())
            .distinct()
            .sorted()
            .toList();
    }

    /**
     * 创建技能请求
     */
    @lombok.Data
    public static class CreateSkillRequest {
        private String skillId;
        private String name;
        private String description;
        private String content;
        private String category;
        private String subCategory;
        private String tags;
        private String customType;
        private String riskLevel = "low";
    }

    /**
     * 更新技能请求
     */
    @lombok.Data
    public static class UpdateSkillRequest {
        private String name;
        private String description;
        private String content;
        private String category;
        private String subCategory;
        private String tags;
        private String customType;
        private String riskLevel;
        private Boolean enabled;
    }
}
