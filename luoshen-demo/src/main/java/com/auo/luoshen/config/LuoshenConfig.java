package com.auo.luoshen.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LuoshenConfig - 洛神系统配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "luoshen")
public class LuoshenConfig {

    /**
     * DashScope API Key
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String modelName = "qwen-max";

    /**
     * Session 存储路径
     */
    private String sessionPath = System.getProperty("user.home") + "/.luoshen/sessions";

}
