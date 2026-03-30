/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.repository.SkillConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Skill 管理 API
 */
@RestController
@RequestMapping("/api/admin/skills")
@RequiredArgsConstructor
public class SkillManagementController {
    
    private final SkillConfigRepository skillConfigRepository;
    
    /**
     * 获取所有 Skill
     */
    @GetMapping
    public List<SkillConfigEntity> listAll() {
        return skillConfigRepository.findAll();
    }
    
    /**
     * 获取单个 Skill
     */
    @GetMapping("/{id}")
    public ResponseEntity<SkillConfigEntity> get(@PathVariable Long id) {
        Optional<SkillConfigEntity> skill = skillConfigRepository.findById(id);
        return skill.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建 Skill
     */
    @PostMapping
    public SkillConfigEntity create(@RequestBody SkillConfigEntity skill) {
        return skillConfigRepository.save(skill);
    }
    
    /**
     * 更新 Skill
     */
    @PutMapping("/{id}")
    public ResponseEntity<SkillConfigEntity> update(@PathVariable Long id, @RequestBody SkillConfigEntity skill) {
        if (!skillConfigRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        skill.setId(id);
        return ResponseEntity.ok(skillConfigRepository.save(skill));
    }
    
    /**
     * 删除 Skill
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!skillConfigRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        skillConfigRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
