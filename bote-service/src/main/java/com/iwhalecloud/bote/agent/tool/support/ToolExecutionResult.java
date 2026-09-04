package com.iwhalecloud.bote.agent.tool.support;

import com.iwhalecloud.bote.common.consts.ChatMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工具调用结果
 *
 * @author bianjp
 * @since 2026-03-17
 */
@Getter
@Setter
@ToString
@Builder
public class ToolExecutionResult {
  /** 是否成功 */
  private boolean success;
  /** 执行结果 */
  private String result;
  /** 耗时(ms) */
  private long spentTime;
  /** 是否直接返回（开启时工具出参作为最终结果，不再调用大模型） */
  private Boolean returnDirect;
  /** SSE 消息类型，不为空时发送 SSE 消息 */
  private ChatMessageType msgType;
  /** SSE 消息内容 */
  private Object msgContent;

  /**
   * 构造成功结果
   */
  public static ToolExecutionResult success(String result) {
    return ToolExecutionResult.builder().success(true).result(result).build();
  }

  /**
   * 构造失败结果
   */
  public static ToolExecutionResult fail(String result) {
    return ToolExecutionResult.builder().success(false).result(result).build();
  }
}
