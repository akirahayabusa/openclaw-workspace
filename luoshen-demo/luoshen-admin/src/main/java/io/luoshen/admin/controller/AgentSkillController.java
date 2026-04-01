/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import io.luoshen.admin.model.AgentSkillRelationEntity;
import io.luoshen.admin.service.SkillBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agent 技能绑定管理 API
 */
@RestController
@RequestMapping("/api/admin/agents")
@Slf4j
@RequiredArgsConstructor
public class AgentSkillController {

    private final SkillBindingService skillBindingService;

    /**
     * 绑定技能到 Agent
     * <p>
     * 请求示例：
     * <pre>
     * POST /api/admin/agents/device-agent/skills/bind
     * Content-Type: application/json
     * {
     *   "skillId": "github",
     *   "bindMode": "custom",
     *   "priority": 0,
     *   "config": {}
     * }
     * </pre>
     *
     * @param agentId  Agent ID
     * @param request  绑定请求
     * @return 绑定关系
     */
    @PostMapping("/{agentId}/skills/bind")
    public ResponseEntity<Map<String, Object>> bindSkill(
        @PathVariable String agentId,
        @RequestBody BindSkillRequest request
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("绑定技能: agent={}, skill={}", agentId, request.getSkillId());

            AgentSkillRelationEntity relation = skillBindingService.bindSkill(
                agentId,
                request.getSkillId(),
                request.getBindMode(),
                request.getPriority(),
                request.getConfig()
            );

            response.put("success", true);
            response.put("message", "技能绑定成功");
            response.put("relation", relation);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("绑定技能失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("绑定技能失败", e);
            response.put("success", false);
            response.put("message", "绑定失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 解绑 Agent 的技能
     * <p>
     * 请求示例：
     * <pre>
     * DELETE /api/admin/agents/device-agent/skills/github
     * </pre>
     *
     * @param agentId Agent ID
     * @param skillId Skill ID
     * @return 操作结果
     */
    @DeleteMapping("/{agentId}/skills/{skillId}")
    public ResponseEntity<Map<String, Object>> unbindSkill(
        @PathVariable String agentId,
        @PathVariable String skillId
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("解绑技能: agent={}, skill={}", agentId, skillId);

            skillBindingService.unbindSkill(agentId, skillId);

            response.put("success", true);
            response.put("message", "技能解绑成功");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("解绑技能失败", e);
            response.put("success", false);
            response.put("message", "解绑失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 查询 Agent 的技能列表
     * <p>
     * 请求示例：
     * <pre>
     * GET /api/admin/agents/device-agent/skills
     * </pre>
     *
     * @param agentId Agent ID
     * @return 技能列表
     */
    @GetMapping("/{agentId}/skills")
    public ResponseEntity<List<Map<String, Object>>> getAgentSkills(@PathVariable String agentId) {
        try {
            List<Map<String, Object>> skills = skillBindingService.getAgentSkills(agentId);
            return ResponseEntity.ok(skills);
        } catch (Exception e) {
            log.error("查询 Agent 技能失败: agentId={}", agentId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量绑定技能
     * <p>
     * 请求示例：
     * <pre>
     * POST /api/admin/agents/device-agent/skills/batch
     * Content-Type: application/json
     * {
     *   "skillIds": ["github", "git"],
     *   "bindMode": "custom"
     * }
     * </pre>
     *
     * @param agentId Agent ID
     * @param request 批量绑定请求
     * @return 绑定关系列表
     */
    @PostMapping("/{agentId}/skills/batch")
    public ResponseEntity<Map<String, Object>> bindSkillsBatch(
        @PathVariable String agentId,
        @RequestBody BatchBindRequest request
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("批量绑定技能: agent={}, count={}", agentId, request.getSkillIds().size());

            List<AgentSkillRelationEntity> relations = skillBindingService.bindSkillsBatch(
                agentId,
                request.getSkillIds(),
                request.getBindMode()
            );

            response.put("success", true);
            response.put("message", String.format("批量绑定完成，成功 %d 个", relations.size()));
            response.put("relations", relations);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("批量绑定技能失败", e);
            response.put("success", false);
            response.put("message", "批量绑定失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 设置 Agent 的技能配置
     * <p>
     * 请求示例：
     * <pre>
     * PUT /api/admin/agents/device-agent/skills/config
     * Content-Type: application/json
     * {
     *   "mode": "custom",
     *   "allowed": ["github", "git"],
     *   "denied": []
     * }
     * </pre>
     *
     * @param agentId Agent ID
     * @param request 配置请求
     * @return 操作结果
     */
    @PutMapping("/{agentId}/skills/config")
    public ResponseEntity<Map<String, Object>> setAgentSkillConfig(
        @PathVariable String agentId,
        @RequestBody SetSkillConfigRequest request
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("设置 Agent 技能配置: agent={}, mode={}", agentId, request.getMode());

            skillBindingService.setAgentSkillConfig(
                agentId,
                request.getMode(),
                request.getAllowed(),
                request.getDenied()
            );

            response.put("success", true);
            response.put("message", "技能配置设置成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("设置技能配置失败", e);
            response.put("success", false);
            response.put("message", "设置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 切换技能绑定状态
     * <p>
     * 请求示例：
     * <pre>
     * POST /api/admin/agents/device-agent/skills/github/toggle
     * </pre>
     *
     * @param agentId Agent ID
     * @param skillId Skill ID
     * @return 新的启用状态
     */
    @PostMapping("/{agentId}/skills/{skillId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleSkillBinding(
        @PathVariable String agentId,
        @PathVariable String skillId,
        @RequestParam(defaultValue = "true") boolean enabled
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            skillBindingService.toggleSkillBinding(agentId, skillId, enabled);

            response.put("success", true);
            response.put("enabled", enabled);
            response.put("message", enabled ? "技能已启用" : "技能已禁用");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("切换技能绑定状态失败", e);
            response.put("success", false);
            response.put("message", "操作失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 删除 Agent 的所有技能绑定
     * <p>
     * 请求示例：
     * <pre>
     * DELETE /api/admin/agents/device-agent/skills
     * </pre>
     *
     * @param agentId Agent ID
     * @return 操作结果
     */
    @DeleteMapping("/{agentId}/skills")
    public ResponseEntity<Map<String, Object>> removeAllAgentSkills(@PathVariable String agentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            skillBindingService.removeAllAgentSkills(agentId);

            response.put("success", true);
            response.put("message", "所有技能绑定已删除");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("删除技能绑定失败", e);
            response.put("success", false);
            response.put("message", "删除失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // ========== 请求 DTO ==========

    /**
     * 绑定技能请求
     */
    @lombok.Data
    public static class BindSkillRequest {
        private String skillId;
        private String bindMode = "custom";
        private Integer priority = 0;
        private String config = "{}";
    }

    /**
     * 批量绑定请求
     */
    @lombok.Data
    public static class BatchBindRequest {
        private List<String> skillIds;
        private String bindMode = "custom";
    }

    /**
     * 设置技能配置请求
     */
    @lombok.Data
    public static class SetSkillConfigRequest {
        private String mode = "custom";
        private List<String> allowed;
        private List<String> denied = new java.util.ArrayList<>();
    }
}
