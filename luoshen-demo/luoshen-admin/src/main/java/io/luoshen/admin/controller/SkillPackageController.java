/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.SkillConfigEntity;
import io.luoshen.admin.service.SkillPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 技能包上传管理 API
 */
@RestController
@RequestMapping("/api/admin/skills")
@Slf4j
@RequiredArgsConstructor
public class SkillPackageController {

    private final SkillPackageService skillPackageService;
    private final SkillQueryService skillQueryService;

    /**
     * 上传技能包
     * <p>
     * 支持两种模式：
     * 1. 纯上传：只上传文件，元数据从 SKILL.md 提取
     * 2. 表单 + 上传：上传文件并提供额外的元数据
     * <p>
     * 请求示例：
     * <pre>
     * curl -X POST http://localhost:9090/api/admin/skills/upload \
     *   -F "file=@github.skill" \
     *   -F "skillId=github" \
     *   -F "name=GitHub 集成" \
     *   -F "description=用于 GitHub 操作..." \
     *   -F "category=dev" \
     *   -F "subCategory=vcs" \
     *   -F 'tags=["github","git","api"]' \
     *   -F "customType="
     * </pre>
     *
     * @param file        技能包文件（.skill 或 .zip）
     * @param skillId     技能 ID（可选，默认自动生成）
     * @param name        技能名称（可选，从 SKILL.md 提取）
     * @param description 技能描述（可选，从 SKILL.md 提取）
     * @param category    主分类（可选）
     * @param subCategory 子分类（可选）
     * @param tags        标签（JSON 数组，可选）
     * @param customType  自定义类型（可选）
     * @return 上传结果
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadSkillPackage(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "skillId", required = false) String skillId,
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "category", required = false) String category,
        @RequestParam(value = "subCategory", required = false) String subCategory,
        @RequestParam(value = "tags", required = false) String tags,
        @RequestParam(value = "customType", required = false) String customType
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("开始上传技能包: {}", file.getOriginalFilename());

            SkillConfigEntity skill = skillPackageService.uploadSkillPackage(
                file, skillId, name, description, category, subCategory, tags, customType
            );

            response.put("success", true);
            response.put("message", "技能包上传成功");
            response.put("skill", skill);

            log.info("技能包上传成功: {}", skill.getSkillId());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("技能包验证失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "技能包验证失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("技能包上传失败", e);
            response.put("success", false);
            response.put("message", "技能包上传失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 创建纯文本技能（不上传文件）
     * <p>
     * 用于创建只包含 Markdown 内容的技能，不需要脚本或资源文件
     * <p>
     * 请求示例：
     * <pre>
     * curl -X POST http://localhost:9090/api/admin/skills/create \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "skillId": "prompt-template",
     *     "name": "提示词模板",
     *     "description": "用于生成提示词...",
     *     "content": "# 技能说明\n...",
     *     "category": "dev",
     *     "tags": ["prompt", "template"]
     *   }'
     * </pre>
     *
     * @param request 技能创建请求
     * @return 创建的技能
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTextSkill(@RequestBody CreateSkillRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("创建纯文本技能: {}", request.getSkillId());

            SkillConfigEntity skill = skillQueryService.createTextSkill(request);

            response.put("success", true);
            response.put("message", "纯文本技能创建成功");
            response.put("skill", skill);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("创建纯文本技能失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("创建纯文本技能失败", e);
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 查询技能列表
     * <p>
     * 支持按分类、来源、启用状态筛选
     * <p>
     * 请求示例：
     * <pre>
     * GET /api/admin/skills?category=dev&enabled=true&page=0&size=20
     * </pre>
     *
     * @param category 分类（可选）
     * @param source   来源（可选）
     * @param enabled  启用状态（可选）
     * @param page     页码（从 0 开始）
     * @param size     每页大小
     * @return 技能列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<SkillConfigEntity>> listSkills(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String source,
        @RequestParam(required = false) Boolean enabled,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<SkillConfigEntity> skills = skillQueryService.listSkills(category, source, enabled, page, size);
        return ResponseEntity.ok(skills);
    }

    /**
     * 获取技能详情
     * <p>
     * 请求示例：
     * <pre>
     * GET /api/admin/skills/github
     * </pre>
     *
     * @param skillId 技能 ID
     * @return 技能详情
     */
    @GetMapping("/{skillId}")
    public ResponseEntity<SkillConfigEntity> getSkill(@PathVariable String skillId) {
        return skillQueryService.getSkill(skillId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新技能
     * <p>
     * 请求示例：
     * <pre>
     * PUT /api/admin/skills/github
     * Content-Type: application/json
     * {
     *   "name": "更新后的名称",
     *   "description": "更新后的描述",
     *   "enabled": false
     * }
     * </pre>
     *
     * @param skillId 技能 ID
     * @param request 更新请求
     * @return 更新后的技能
     */
    @PutMapping("/{skillId}")
    public ResponseEntity<Map<String, Object>> updateSkill(
        @PathVariable String skillId,
        @RequestBody UpdateSkillRequest request
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            SkillConfigEntity skill = skillQueryService.updateSkill(skillId, request);
            
            response.put("success", true);
            response.put("message", "技能更新成功");
            response.put("skill", skill);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除技能
     * <p>
     * 请求示例：
     * <pre>
     * DELETE /api/admin/skills/github
     * </pre>
     *
     * @param skillId 技能 ID
     * @return 操作结果
     */
    @DeleteMapping("/{skillId}")
    public ResponseEntity<Map<String, Object>> deleteSkill(@PathVariable String skillId) {
        Map<String, Object> response = new HashMap<>();

        try {
            skillQueryService.deleteSkill(skillId);
            
            response.put("success", true);
            response.put("message", "技能删除成功");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 切换技能启用状态
     * <p>
     * 请求示例：
     * <pre>
     * POST /api/admin/skills/github/toggle
     * </pre>
     *
     * @param skillId 技能 ID
     * @return 新的启用状态
     */
    @PostMapping("/{skillId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleSkillEnabled(@PathVariable String skillId) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean enabled = skillQueryService.toggleSkillEnabled(skillId);
            
            response.put("success", true);
            response.put("enabled", enabled);
            response.put("message", enabled ? "技能已启用" : "技能已禁用");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 搜索技能
     * <p>
     * 请求示例：
     * <pre>
     * GET /api/admin/skills/search?keyword=github
     * </pre>
     *
     * @param keyword 搜索关键词
     * @return 匹配的技能列表
     */
    @GetMapping("/search")
    public ResponseEntity<List<SkillConfigEntity>> searchSkills(@RequestParam String keyword) {
        List<SkillConfigEntity> skills = skillQueryService.searchSkills(keyword);
        return ResponseEntity.ok(skills);
    }

    /**
     * 获取所有分类
     * <p>
     * 请求示例：
     * <pre>
     * GET /api/admin/skills/categories
     * </pre>
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = skillQueryService.getAllCategories();
        return ResponseEntity.ok(categories);
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
}
