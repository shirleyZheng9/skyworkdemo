package com.iwhalecloud.bote.agent.event;

import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;

/**
 * 大模型流式输出结束事件
 *
 * @param response 收集的完整响应
 * @author bianjp
 * @since 2026-03-19
 */
public record StreamResponseEndEvent(ChatCompletionResponse response) implements AgentEvent {
}
