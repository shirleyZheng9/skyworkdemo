package com.iwhalecloud.bote.agent.event;

import com.iwhalecloud.bote.common.sse.event.ToolCallEvent;

/**
 * 工具调用开始事件
 *
 * @author bianjp
 * @since 2026-03-09
 */
public record ToolCallStartEvent(ToolCallEvent toolCallEvent) implements AgentEvent {
}
