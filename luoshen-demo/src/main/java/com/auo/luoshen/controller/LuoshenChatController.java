package com.auo.luoshen.controller;

import com.auo.luoshen.service.LuoshenSessionService;
import com.auo.luoshen.util.MsgUtils;
import io.agentscope.core.message.Msg;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

/**
 * LuoshenChatController - 洛神系统聊天接口控制器
 */
@RestController
@RequestMapping("/api/luoshen")
public class LuoshenChatController {

    private final LuoshenSessionService sessionService;

    public LuoshenChatController(LuoshenSessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * 发送消息
     */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        Msg userMsg = MsgUtils.createUserMessage(request.getMessage());
        Msg response = sessionService.sendMessage(request.getSessionId(), userMsg);

        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setSessionId(request.getSessionId());
        chatResponse.setReply(MsgUtils.getTextContent(response));
        return chatResponse;
    }

    /**
     * 清除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public void clearSession(@PathVariable String sessionId) {
        sessionService.clearSession(sessionId);
    }

    @Data
    public static class ChatRequest {
        private String sessionId;
        private String message;
    }

    @Data
    public static class ChatResponse {
        private String sessionId;
        private String reply;
    }

}
