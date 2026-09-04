package com.iwhalecloud.bote.dto.chat;

/**
 * WebSocket 对话接口的事件对象
 *
 * @param id 事件 ID
 * @param type 事件类型
 * @param payload 事件负载
 * @author bianjp
 * @since 2026-03-23
 */
public record ChatWebSocketEvent(String id, String type, Object payload) {

  /**
   * 工具调用响应
   *
   * @param success 是否成功
   * @param result 执行结果
   * @param error 错误信息
   */
  public record ToolCallResponse(Boolean success, String result, String error) {
  }
}

