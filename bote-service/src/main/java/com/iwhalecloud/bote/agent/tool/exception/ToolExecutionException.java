package com.iwhalecloud.bote.agent.tool.exception;

import java.io.Serial;

/**
 * 工具执行异常
 *
 * @author bianjp
 * @since 2026-03-24
 */
public class ToolExecutionException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  /**
   * 构造工具执行异常
   *
   * @param message 对大模型友好的错误信息，用于返回给大模型
   */
  public ToolExecutionException(String message) {
    super(message);
  }

  /**
   * 构造工具执行异常
   *
   * @param message 对大模型友好的错误信息，用于返回给大模型
   * @param cause 原始异常
   */
  public ToolExecutionException(String message, Throwable cause) {
    super(message, cause);
  }

}
