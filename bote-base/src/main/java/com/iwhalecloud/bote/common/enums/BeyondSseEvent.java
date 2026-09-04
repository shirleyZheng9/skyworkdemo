package com.iwhalecloud.bote.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * 百应 SSE 事件类型
 *
 * @author bianjp
 * @since 2025-07-19
 */
@Getter
@RequiredArgsConstructor
public enum BeyondSseEvent {
  /** 进度 */
  MODULE_STATUS("moduleStatus"),
  /** 推理⽇志增量响应开始 */
  REASONING_START("reasoningLogStart"),
  /** 推理⽇志增量响应 */
  REASONING_DELTA("reasoningLogDelta"),
  /** 推理⽇志结束响应 */
  REASONING_END("reasoningLogEnd"),
  /** 回答内容增量响应 */
  ANSWER_DELTA("answerDelta"),
  /** 回答内容结束响应 */
  ANSWER_END("answerEnd"),
  /** 异常 */
  ERROR("error"),
  /** 应⽤流式响应结束 */
  END("appStreamResponse");

  /** 事件编码 */
  private final String code;

  /**
   * 根据编码获取事件
   */
  @Nullable
  public static BeyondSseEvent of(@Nullable String code) {
    if (StringUtils.isEmpty(code)) {
      return null;
    }
    for (BeyondSseEvent value : values()) {
      if (code.equals(value.code)) {
        return value;
      }
    }
    return null;
  }
}
