package com.auo.luoshen.service;

import com.auo.luoshen.agent.LuoshenAgentFactory;
import com.auo.luoshen.config.LuoshenConfig;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.memory.Memory;
import io.agentscope.core.message.Msg;
import io.agentscope.core.session.JsonSession;
import io.agentscope.core.session.Session;
import io.agentscope.core.state.SimpleSessionKey;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LuoshenSessionService - 洛神系统会话管理服务
 *
 * 负责：
 * - 用户会话的创建和管理
 * - 智能体的会话持久化
 * - 记忆管理
 */
@Service
public class LuoshenSessionService {

    private final LuoshenConfig config;
    private final LuoshenAgentFactory agentFactory;

    private final Map<String, ReActAgent> agentCache = new ConcurrentHashMap<>();
    private final Session session;

    public LuoshenSessionService(LuoshenConfig config, LuoshenAgentFactory agentFactory) {
        this.config = config;
        this.agentFactory = agentFactory;

        Path sessionPath = Paths.get(config.getSessionPath());
        this.session = new JsonSession(sessionPath);
    }

    /**
     * 获取或创建用户的 Leader Agent
     */
    public ReActAgent getOrCreateLeaderAgent(String sessionId) {
        return agentCache.computeIfAbsent(sessionId, id -> {
            ReActAgent agent = agentFactory.createLeaderAgent();

            // 加载已有会话
            if (session.exists(SimpleSessionKey.of(id))) {
                agent.loadFrom(session, id);
            }

            return agent;
        });
    }

    /**
     * 发送消息给 Leader Agent 并获取回复
     */
    public Msg sendMessage(String sessionId, Msg userMessage) {
        ReActAgent agent = getOrCreateLeaderAgent(sessionId);

        Msg response = agent.call(userMessage).block();

        // 保存会话
        saveSession(sessionId, agent);

        return response;
    }

    /**
     * 保存会话
     */
    public void saveSession(String sessionId, ReActAgent agent) {
        agent.saveTo(session, sessionId);
    }

    /**
     * 获取会话记忆
     */
    public Memory getSessionMemory(String sessionId) {
        ReActAgent agent = getOrCreateLeaderAgent(sessionId);
        return agent.getMemory();
    }

    /**
     * 清除会话
     */
    public void clearSession(String sessionId) {
        agentCache.remove(sessionId);
        if (session.exists(SimpleSessionKey.of(sessionId))) {
            session.delete(SimpleSessionKey.of(sessionId));
        }
    }

}
