/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.service;

import io.agentscope.core.skill.AgentSkill;
import io.agentscope.core.skill.repository.FileSystemSkillRepository;
import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.SkillConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Skill 动态管理服务
 * 
 * 核心功能：
 * 1. 从数据库读取配置，动态加载 Skill
 * 2. 支持热加载，修改后即时生效
 * 3. Skill 文件同步管理
 */
@Slf4j
@Service
public class SkillDynamicService {
    
    private final SkillConfigRepository skillConfigRepository;
    private final Path skillStoragePath;
    private final FileSystemSkillRepository fileRepository;
    
    // Skill 缓存
    private final Map<String, AgentSkill> skillCache = new ConcurrentHashMap<>();
    
    public SkillDynamicService(
            SkillConfigRepository skillConfigRepository,
            @Value("${luoshen.skill.storage-path:${user.home}/.luoshen/skills}") String storagePath) {
        this.skillConfigRepository = skillConfigRepository;
        this.skillStoragePath = Paths.get(storagePath);
        this.fileRepository = new FileSystemSkillRepository(skillStoragePath, false);
        
        // 确保目录存在
        try {
            Files.createDirectories(skillStoragePath);
        } catch (Exception e) {
            log.error("创建 Skill 存储目录失败", e);
        }
    }
    
    /**
     * 创建新的 Skill
     */
    @Transactional
    public SkillConfigEntity createSkill(SkillConfigEntity skill) {
        if (skillConfigRepository.findBySkillId(skill.getSkillId()).isPresent()) {
            throw new IllegalArgumentException("Skill ID 已存在: " + skill.getSkillId());
        }
        
        SkillConfigEntity saved = skillConfigRepository.save(skill);
        log.info("创建 Skill: {}", saved.getSkillId());
        
        // 同步到文件系统
        syncSkillToFile(saved);
        
        // 加载到缓存
        loadSkillToCache(saved);
        
        return saved;
    }
    
    /**
     * 更新 Skill
     * 
     * 更新后立即重新加载，配置即时生效
     */
    @Transactional
    public SkillConfigEntity updateSkill(String skillId, SkillConfigEntity newSkill) {
        SkillConfigEntity existing = skillConfigRepository.findBySkillId(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill 不存在: " + skillId));
        
        // 更新字段
        existing.setName(newSkill.getName());
        existing.setDescription(newSkill.getDescription());
        existing.setContent(newSkill.getContent());
        existing.setType(newSkill.getType());
        existing.setAgentId(newSkill.getAgentId());
        existing.setEnabled(newSkill.getEnabled());
        existing.setVersion(newSkill.getVersion());
        
        SkillConfigEntity saved = skillConfigRepository.save(existing);
        log.info("更新 Skill: {}", saved.getSkillId());
        
        // 清除缓存，重新加载
        skillCache.remove(skillId);
        
        // 同步到文件系统
        syncSkillToFile(saved);
        
        if (saved.getEnabled()) {
            loadSkillToCache(saved);
        }
        
        return saved;
    }
    
    /**
     * 删除 Skill
     */
    @Transactional
    public void deleteSkill(String skillId) {
        skillCache.remove(skillId);
        skillConfigRepository.deleteBySkillId(skillId);
        
        // 删除文件
        try {
            Path skillFile = skillStoragePath.resolve(skillId).resolve("SKILL.md");
            Files.deleteIfExists(skillFile);
        } catch (Exception e) {
            log.warn("删除 Skill 文件失败: {}", skillId, e);
        }
        
        log.info("删除 Skill: {}", skillId);
    }
    
    /**
     * 获取 Skill
     */
    public AgentSkill getSkill(String skillId) {
        return skillCache.computeIfAbsent(skillId, id -> {
            SkillConfigEntity config = skillConfigRepository.findBySkillId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Skill 不存在: " + id));
            
            if (!config.getEnabled()) {
                throw new IllegalStateException("Skill 已禁用: " + id);
            }
            
            return loadSkillToCache(config);
        });
    }
    
    /**
     * 获取所有启用的 Skill
     */
    public List<AgentSkill> getAllEnabledSkills() {
        List<SkillConfigEntity> configs = skillConfigRepository.findByEnabled(true);
        return configs.stream()
                .map(c -> getSkill(c.getSkillId()))
                .toList();
    }
    
    /**
     * 刷新所有 Skill
     */
    public void refreshAllSkills() {
        skillCache.clear();
        List<SkillConfigEntity> configs = skillConfigRepository.findByEnabled(true);
        configs.forEach(this::loadSkillToCache);
        log.info("刷新所有 Skill，共 {} 个", configs.size());
    }
    
    /**
     * 同步 Skill 到文件系统
     */
    private void syncSkillToFile(SkillConfigEntity skill) {
        try {
            Path skillDir = skillStoragePath.resolve(skill.getSkillId());
            Files.createDirectories(skillDir);
            
            Path skillFile = skillDir.resolve("SKILL.md");
            
            // 构建 SKILL.md 内容
            StringBuilder content = new StringBuilder();
            content.append("---\n");
            content.append("name: ").append(skill.getName()).append("\n");
            content.append("description: ").append(skill.getDescription() != null ? skill.getDescription() : "").append("\n");
            content.append("---\n\n");
            content.append(skill.getContent() != null ? skill.getContent() : "");
            
            Files.writeString(skillFile, content.toString());
            log.info("同步 Skill 文件: {}", skillFile);
            
        } catch (Exception e) {
            log.error("同步 Skill 文件失败: {}", skill.getSkillId(), e);
        }
    }
    
    /**
     * 加载 Skill 到缓存
     */
    private AgentSkill loadSkillToCache(SkillConfigEntity config) {
        try {
            AgentSkill skill = fileRepository.getSkill(config.getSkillId());
            if (skill != null) {
                skillCache.put(config.getSkillId(), skill);
                log.info("加载 Skill 到缓存: {}", config.getSkillId());
            }
            return skill;
        } catch (Exception e) {
            log.error("加载 Skill 失败: {}", config.getSkillId(), e);
            return null;
        }
    }
    
    /**
     * 获取 Skill 配置列表
     */
    public List<SkillConfigEntity> listAllConfigs() {
        return skillConfigRepository.findAll();
    }
    
    /**
     * 获取 Skill 配置
     */
    public SkillConfigEntity getConfig(String skillId) {
        return skillConfigRepository.findBySkillId(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill 不存在: " + skillId));
    }
}