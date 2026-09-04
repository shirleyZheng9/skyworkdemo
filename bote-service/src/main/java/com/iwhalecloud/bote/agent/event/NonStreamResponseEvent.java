package com.iwhalecloud.bote.agent.event;

import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;

/**
 * 非流式输出的大模型响应事件
 *
 * @author bianjp
 * @since 2026-03-09
 */
public record NonStreamResponseEvent(ChatCompletionResponse response) implements AgentEvent {
}
