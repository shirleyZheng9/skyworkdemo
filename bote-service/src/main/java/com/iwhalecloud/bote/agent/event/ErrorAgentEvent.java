package com.iwhalecloud.bote.agent.event;

import org.springframework.lang.Nullable;

/**
 * 执行失败的通用智能体事件
 *
 * @param message 错误信息
 * @param exception 异常
 * @author bianjp
 * @since 2026-03-19
 */
public record ErrorAgentEvent(String message, @Nullable Exception exception) implements AgentEvent {
}
