package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回复项实体
 * 迁移对应关系: Go语言entity.ReplyItem
 * - 功能: 存储回复项的具体内容
 * - 字段定义:
 * * Message: *Message - 消息内容
 * * FinishReason: string - 完成原因
 * * TokenUsage: *TokenUsage - Token使用情况
 * <p>
 * Java实现说明:
 * - 对应Go的entity.ReplyItem结构体
 * - 使用Java类定义，包含回复项字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go string -> Java String
 * - Go指针类型 -> Java对象引用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyItem {

  /**
   * 消息内容
   * 迁移对应关系: Go语言entity.ReplyItem.Message (*Message)
   * - 功能: 回复的消息内容
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 存储AI生成的消息
   */
  @JsonProperty("message")
  private Message message;

  /**
   * 完成原因
   * 迁移对应关系: Go语言entity.ReplyItem.FinishReason (string)
   * - 功能: 生成完成的原因
   * - 类型: Go的string对应Java的String
   * - 用途: 标识生成结束的原因
   */
  @JsonProperty("finish_reason")
  private String finishReason;

  /**
   * Token使用情况
   * 迁移对应关系: Go语言entity.ReplyItem.TokenUsage (*TokenUsage)
   * - 功能: Token使用统计信息
   * - 类型: Go的指针类型对应Java的对象引用
   * - 用途: 统计输入输出Token数量
   */
  @JsonProperty("token_usage")
  private TokenUsage tokenUsage;
}
