package com.iwhalecloud.bote.llm.client.dto;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * SSE 事件
 *
 * @param id 事件 ID
 * @param event 事件名称
 * @param data 事件数据
 * @author bianjp
 * @since 2025-12-17
 */
public record ServerSentEvent(@Nullable String id, @Nullable String event, @NonNull String data) {
}
