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
package io.luoshen.leader;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;

/**
 * Leader Agent 应用入口
 * 
 * 提供总控智能体的 REST API 接口
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.luoshen.core", "io.luoshen.leader"})
public class LeaderAgentApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LeaderAgentApplication.class, args);
    }
    
    @RestController
    @RequestMapping("/api/leader")
    public static class LeaderController {
        
        @Autowired
        @Qualifier("leaderAgent")
        private ReActAgent leaderAgent;
        
        @Autowired
        private Session luoshenSession;
        
        /**
         * 处理用户请求
         * 
         * @param request 用户请求内容
         * @param sessionId 会话 ID（可选）
         * @return Agent 响应
         */
        @PostMapping("/chat")
        public ChatResponse chat(@RequestBody ChatRequest request,
                                  @RequestParam(value = "sessionId", required = false) String sessionId) {
            try {
                // 加载会话（如果存在）
                if (sessionId != null && !sessionId.isEmpty()) {
                    leaderAgent.loadIfExists(luoshenSession, sessionId);
                }
                
                // 构建用户消息
                Msg userMsg = Msg.builder()
                        .role(MsgRole.USER)
                        .content(TextBlock.builder().text(request.message()).build())
                        .build();
                
                // 调用 Agent
                Msg response = leaderAgent.call(userMsg).block();
                
                // 保存会话
                if (sessionId != null && !sessionId.isEmpty()) {
                    leaderAgent.saveTo(luoshenSession, sessionId);
                }
                
                // 返回响应
                return new ChatResponse(
                        response != null ? response.getTextContent() : "无响应",
                        sessionId,
                        response != null ? response.getGenerateReason().name() : "UNKNOWN"
                );
                
            } catch (Exception e) {
                return new ChatResponse("处理请求时发生错误: " + e.getMessage(), sessionId, "ERROR");
            }
        }
        
        /**
         * 清除会话
         */
        @DeleteMapping("/session/{sessionId}")
        public void clearSession(@PathVariable String sessionId) {
            leaderAgent.getMemory().clear();
            // 会话文件会被新会话覆盖
        }
    }
    
    /**
     * 聊天请求
     */
    public static record ChatRequest(String message) {}
    
    /**
     * 聊天响应
     */
    public static record ChatResponse(String response, String sessionId, String reason) {}
}