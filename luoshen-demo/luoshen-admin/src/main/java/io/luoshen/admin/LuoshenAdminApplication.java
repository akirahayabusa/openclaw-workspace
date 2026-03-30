/*
 * Copyright 2024-2026 Luoshen Team
 */
package io.luoshen.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Luoshen Admin - 洛神系统管理后台
 * 
 * 提供动态管理能力：
 * - Agent 动态配置和管理
 * - Skill 动态加载和管理
 * - MCP 工具动态注册和管理
 * - Session 会话管理
 * - Memory 记忆管理
 * 
 * 所有配置存储在数据库中，修改后即时生效
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.luoshen.core", "io.luoshen.admin"})
public class LuoshenAdminApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LuoshenAdminApplication.class, args);
    }
}