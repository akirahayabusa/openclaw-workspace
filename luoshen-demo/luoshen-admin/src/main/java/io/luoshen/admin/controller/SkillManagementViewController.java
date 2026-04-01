/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 技能管理页面视图控制器
 */
@Controller
public class SkillManagementViewController {

    /**
     * 技能包上传页面
     */
    @GetMapping("/skills/upload")
    public String uploadPage() {
        return "skill-upload";
    }

    /**
     * 技能管理列表页面
     */
    @GetMapping("/skills")
    public String listPage() {
        return "skills-list";
    }
}
