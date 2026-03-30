package com.auo.luoshen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LuoshenDemoApplication - 洛神系统 AI 应用平台 Demo 主入口
 *
 * 架构：
 * - Leader/Master Agent: 总控智能体，负责任务分发和协调
 * - Core Agent: 设备智能体，负责设备相关操作
 * - Sub Agent: 质量智能体、物料智能体等，负责具体领域任务
 */
@SpringBootApplication
public class LuoshenDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuoshenDemoApplication.class, args);
    }

}
