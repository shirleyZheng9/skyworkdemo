package com.iwhalecloud.bote.agent.event;

import com.iwhalecloud.bote.common.sse.event.ToolCallResultEvent;

/**
 * 工具调用结束事件
 *
 * @author bianjp
 * @since 2026-03-09
 */
public record ToolCallEndEvent(ToolCallResultEvent toolCallResultEvent) implements AgentEvent {
}
