package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回复项实体
 * 迁移对应关系: Go语言ReplyItem
 * - 功能: 回复项数据结构
 * - 字段: content, reasoningContent, toolCalls, finishReason, tokenUsage
 * <p>
 * Java实现说明:
 * - 对应Go的ReplyItem结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *string -> Java String
 * - Go []*ToolCall -> Java List<ToolCall>
 * - Go string -> Java String
 * - Go *TokenUsage -> Java TokenUsage
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyItem {
  @JsonProperty("content")
  private String content;

  @JsonProperty("reasoning_content")
  private String reasoningContent;

  @JsonProperty("tool_calls")
  private List<ToolCall> toolCalls;

  @JsonProperty("finish_reason")
  private String finishReason;

  @JsonProperty("token_usage")
  private TokenUsage tokenUsage;
}
