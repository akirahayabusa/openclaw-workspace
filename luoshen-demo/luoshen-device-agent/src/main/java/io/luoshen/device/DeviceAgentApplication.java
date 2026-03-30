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
package io.luoshen.device;

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
 * Device Agent 应用入口
 * 
 * 提供设备智能体的 REST API 接口
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.luoshen.core", "io.luoshen.device"})
public class DeviceAgentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DeviceAgentApplication.class, args);
    }
    
    @RestController
    @RequestMapping("/api/device")
    public static class DeviceController {
        
        @Autowired
        @Qualifier("deviceAgent")
        private ReActAgent deviceAgent;
        
        /**
         * 处理设备相关请求
         */
        @PostMapping("/chat")
        public ChatResponse chat(@RequestBody ChatRequest request) {
            try {
                Msg userMsg = Msg.builder()
                        .role(MsgRole.USER)
                        .content(TextBlock.builder().text(request.getMessage()).build())
                        .build();
                
                Msg response = deviceAgent.call(userMsg).block();
                
                return new ChatResponse(
                        response != null ? response.getTextContent() : "无响应",
                        response != null ? response.getGenerateReason().name() : "UNKNOWN"
                );
                
            } catch (Exception e) {
                return new ChatResponse("处理请求时发生错误: " + e.getMessage(), "ERROR");
            }
        }
        
        /**
         * 查询设备状态（直接调用工具）
         */
        @GetMapping("/status/{deviceId}")
        public String getStatus(@PathVariable String deviceId) {
            // 这里可以直接调用工具，也可以通过 Agent 处理
            Msg userMsg = Msg.builder()
                    .role(MsgRole.USER)
                    .textContent("查询设备 " + deviceId + " 的状态")
                    .build();
            
            Msg response = deviceAgent.call(userMsg).block();
            return response != null ? response.getTextContent() : "无响应";
        }
    }
    
    public static record ChatRequest(String message) {}
    public static record ChatResponse(String response, String reason) {}
}