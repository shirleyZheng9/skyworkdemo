package com.iwhalecloud.bote.agent.event;

import com.iwhalecloud.bote.common.consts.ChatMessageType;

/**
 * SSE 事件
 *
 * <p>用于向前端发送 SSE 事件</p>
 *
 * @author bianjp
 * @since 2026-03-17
 */
public record SseAgentEvent(ChatMessageType msgType, Object msgContent) implements AgentEvent {

}
