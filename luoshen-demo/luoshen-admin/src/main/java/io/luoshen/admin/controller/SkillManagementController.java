/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.service.SkillDynamicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Skill 管理 API
 */
@RestController
@RequestMapping("/api/admin/skills")
@RequiredArgsConstructor
public class SkillManagementController {
    
    private final SkillDynamicService skillDynamicService;
    
    @GetMapping
    public List<SkillConfigEntity> listAll() {
        return skillDynamicService.listAllConfigs();
    }
    
    @GetMapping("/{skillId}")
    public SkillConfigEntity get(@PathVariable String skillId) {
        return skillDynamicService.getConfig(skillId);
    }
    
    @PostMapping
    public SkillConfigEntity create(@RequestBody SkillConfigEntity skill) {
        return skillDynamicService.createSkill(skill);
    }
    
    @PutMapping("/{skillId}")
    public SkillConfigEntity update(@PathVariable String skillId, @RequestBody SkillConfigEntity skill) {
        return skillDynamicService.updateSkill(skillId, skill);
    }
    
    @DeleteMapping("/{skillId}")
    public ResponseEntity<Void> delete(@PathVariable String skillId) {
        skillDynamicService.deleteSkill(skillId);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAll() {
        skillDynamicService.refreshAllSkills();
        return ResponseEntity.ok("刷新成功");
    }
}