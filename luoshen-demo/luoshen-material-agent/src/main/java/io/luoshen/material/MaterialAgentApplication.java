/*
 * Copyright 2024-2026 Luoshen Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.luoshen.material;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

/**
 * Material Agent 应用入口
 * 
 * 提供物料智能体的 REST API 接口
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.luoshen.core", "io.luoshen.material"})
public class MaterialAgentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MaterialAgentApplication.class, args);
    }
    
    @RestController
    @RequestMapping("/api/material")
    public static class MaterialController {
        
        @Autowired
        @Qualifier("materialAgent")
        private ReActAgent materialAgent;
        
        /**
         * 处理物料相关请求
         */
        @PostMapping("/chat")
        public ChatResponse chat(@RequestBody ChatRequest request) {
            try {
                Msg userMsg = Msg.builder()
                        .role(MsgRole.USER)
                        .content(TextBlock.builder().text(request.message()).build())
                        .build();
                
                Msg response = materialAgent.call(userMsg).block();
                
                return new ChatResponse(
                        response != null ? response.getTextContent() : "无响应",
                        response != null ? response.getGenerateReason().name() : "UNKNOWN"
                );
                
            } catch (Exception e) {
                return new ChatResponse("处理请求时发生错误: " + e.getMessage(), "ERROR");
            }
        }
        
        /**
         * 查询物料库存（直接调用工具）
         */
        @GetMapping("/inventory/{materialId}")
        public String getInventory(@PathVariable String materialId) {
            Msg userMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .textContent("查询物料 " + materialId + " 的库存信息")
                    .build();
            
            Msg response = materialAgent.call(userMsg).block();
            return response != null ? response.getTextContent() : "无响应";
        }
    }
    
    public static record ChatRequest(String message) {}
    public static record ChatResponse(String response, String reason) {}
}