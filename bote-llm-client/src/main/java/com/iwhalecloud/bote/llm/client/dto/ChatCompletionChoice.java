package com.iwhalecloud.bote.llm.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.llm.client.dto.message.AssistantMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 会话补全选项
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public class ChatCompletionChoice {
  /** 编号，从 0 开始 */
  private Integer index;
  /** 结束原因(stop: 自然停止或触发入参中的 stop 条件, length: 请求中的 token 数量过长， content_filter: 由于内容过滤器中的标志而忽略了内容, tool_calls: 需要调用工具) */
  private String finishReason;
  /** 非流式响应消息 */
  private AssistantMessage message;
  /** 流式响应消息 */
  private AssistantMessage delta;
}
