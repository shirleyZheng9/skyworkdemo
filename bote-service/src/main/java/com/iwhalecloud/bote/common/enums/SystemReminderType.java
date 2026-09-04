package com.iwhalecloud.bote.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 系统提醒类型
 *
 * @author bianjp
 * @since 2026-04-13
 */
@RequiredArgsConstructor
@Getter
public enum SystemReminderType {
  /** 任务提醒 */
  TASK_REMINDER("task_reminder", true),
  /** 会话状态提醒，不需要持久化，每次调用大模型时传递最新数据 */
  SESSION_STATE_REMINDER("session_state_reminder", false);

  /** 编码 */
  private final String code;
  /** 是否持久化（存储到会话表，且加载历史消息时也使用） */
  private final boolean persistent;

  /**
   * 获取类型编码
   *
   * <p>添加 JsonValue 注解以使 Jackson 使用类型编码作为 JSON 序列化的结果（默认会使用枚举值名称，但名称是大写的不太方便）</p>
   */
  @JsonValue
  public String getCode() {
    return code;
  }
}
