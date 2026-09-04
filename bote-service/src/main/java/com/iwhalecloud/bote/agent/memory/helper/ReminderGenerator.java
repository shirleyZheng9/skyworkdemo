package com.iwhalecloud.bote.agent.memory.helper;

import com.iwhalecloud.bote.dto.SystemReminder;
import com.iwhalecloud.bote.dto.agent.MemoryMessage;
import com.iwhalecloud.bote.llm.client.dto.message.Message;
import java.util.List;
import org.springframework.lang.Nullable;

/**
 * 系统提醒生成器
 *
 * @author bianjp
 * @since 2026-04-13
 */
public interface ReminderGenerator {

  /**
   * 根据消息列表生成系统提醒
   *
   * @param messages 历史消息列表
   * @param message 新消息
   * @return 系统提醒。没有时返回 null
   */
  @Nullable
  SystemReminder generate(List<MemoryMessage> messages, Message message);

}
