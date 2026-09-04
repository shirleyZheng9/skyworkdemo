package com.iwhalecloud.bote.llm.client.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话补全响应中的错误信息
 *
 * @author bianjp
 * @since 2024-08-06
 */
@Getter
@Setter
@ToString
public class ChatCompletionError {
  /** 错误类型 */
  private String type;
  /** 错误编码 */
  private String code;
  /** 错误信息 */
  private String message;
  /** 参数路径 */
  private String param;
}
