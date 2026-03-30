package com.auo.luoshen.util;

import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;

/**
 * MsgUtils - 消息工具类
 */
public class MsgUtils {

    private MsgUtils() {
    }

    /**
     * 创建用户消息
     */
    public static Msg createUserMessage(String text) {
        return Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(text).build())
                .build();
    }

    /**
     * 获取消息文本内容
     */
    public static String getTextContent(Msg msg) {
        if (msg == null || msg.getContent() == null) {
            return "";
        }
        if (msg.getContent() instanceof TextBlock) {
            return ((TextBlock) msg.getContent()).getText();
        }
        return msg.getContent().toString();
    }

}
