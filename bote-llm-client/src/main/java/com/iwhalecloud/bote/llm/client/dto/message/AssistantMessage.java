package com.iwhalecloud.bote.llm.client.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.iwhalecloud.bote.llm.client.consts.MessageRole;
import com.iwhalecloud.bote.llm.client.dto.FunctionCall;
import com.iwhalecloud.bote.llm.client.dto.ToolCall;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

/**
 * 助手消息
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString(callSuper = true)
@JsonInclude(Include.NON_NULL)
@JsonNaming(SnakeCaseStrategy.class)
public class AssistantMessage extends Message {
  /** 消息内容。通义千问即使有 functionCall 也要求必须传 content */
  @JsonInclude
  private String content;
  /** 推理内容 */
  private String reasoningContent;
  /** 工具调用列表 */
  private List<ToolCall> toolCalls;
  /** 函数调用。已废弃，请使用 toolCalls */
  private FunctionCall functionCall;

  public AssistantMessage() {
    super(MessageRole.ASSISTANT);
  }

  public AssistantMessage(String content) {
    this();
    this.content = content;
  }

  public AssistantMessage(ToolCall toolCall) {
    this();
    // 避免 content=null 导致调用外部接口时报错
    this.content = "";
    this.toolCalls = Collections.singletonList(toolCall);
  }

  public AssistantMessage(List<ToolCall> toolCalls) {
    this();
    // 避免 content=null 导致调用外部接口时报错
    this.content = "";
    this.toolCalls = toolCalls;
  }

  /**
   * 检查消息内容是否为空
   */
  @JsonIgnore
  public boolean isEmpty() {
    return StringUtils.isEmpty(content) && StringUtils.isEmpty(reasoningContent);
  }

  /**
   * 是否包含工具调用
   *
   * <p>兼容 toolCalls 和 functionCall</p>
   */
  @JsonIgnore
  public boolean hasToolCall() {
    return CollectionUtils.isNotEmpty(toolCalls) || functionCall != null;
  }

  /**
   * 获取工具调用
   *
   * <p>兼容 toolCalls 和 functionCall</p>
   */
  @JsonIgnore
  public ToolCall getToolCall() {
    Assert.isTrue(hasToolCall(), "没有工具调用");
    if (CollectionUtils.isNotEmpty(toolCalls)) {
      Assert.isTrue(toolCalls.size() == 1, "不支持并行工具调用");
      return toolCalls.get(0);
    }
    return new ToolCall(functionCall.getName(), functionCall);
  }
}
