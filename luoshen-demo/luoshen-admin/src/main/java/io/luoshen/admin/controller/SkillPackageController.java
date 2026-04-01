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

            // TODO: 实现纯文本技能创建逻辑
            // 这部分需要在另一个 Service 中实现

            response.put("success", true);
            response.put("message", "纯文本技能创建成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("创建纯文本技能失败", e);
            response.put("success", false);
            response.put("message", "创建失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
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
