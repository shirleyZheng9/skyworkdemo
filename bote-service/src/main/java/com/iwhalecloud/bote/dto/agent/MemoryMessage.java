package com.iwhalecloud.bote.dto.agent;

import com.iwhalecloud.bote.dto.SystemReminder;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 记忆消息对象
 *
 * <p>用于记录历史消息对应的消息 ID, 方便做记忆的压缩</p>
 *
 * @param msgId 消息 ID
 * @param message 消息
 * @param reminders 系统提醒
 * @since 2026-04-13
 */
public record MemoryMessage(@Nullable Long msgId, Message message, @Nullable List<SystemReminder> reminders) {
  public MemoryMessage(@Nullable Long msgId, Message message) {
    this(msgId, message, null);
  }
}
