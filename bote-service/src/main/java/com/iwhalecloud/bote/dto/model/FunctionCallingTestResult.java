package com.iwhalecloud.bote.dto.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionRequest;
import com.iwhalecloud.bote.llm.client.dto.ChatCompletionResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 函数调用测试结果
 *
 * @author bianjp
 * @since 2025-12-17
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class FunctionCallingTestResult {
  /** 是否成功 */
  private Boolean success;
  /** 错误信息 */
  private String message;
  /** 第一轮请求 */
  private ChatCompletionRequest request1;
  /** 第一轮响应 */
  private ChatCompletionResponse response1;
  /** 第二轮请求 */
  private ChatCompletionRequest request2;
  /** 第二轮响应 */
  private ChatCompletionResponse response2;

  /**
   * 设置失败
   */
  public void fail(String error) {
    this.success = false;
    this.message = error;
  }
}
