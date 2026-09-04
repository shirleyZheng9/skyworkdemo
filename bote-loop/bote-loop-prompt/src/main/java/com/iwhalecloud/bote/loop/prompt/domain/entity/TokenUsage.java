package com.iwhalecloud.bote.loop.prompt.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Token使用情况实体
 * 迁移对应关系: Go语言entity.TokenUsage
 * - 功能: 存储Token使用统计信息
 * - 字段定义:
 * * InputTokens: int64 - 输入Token数量
 * * OutputTokens: int64 - 输出Token数量
 * <p>
 * Java实现说明:
 * - 对应Go的entity.TokenUsage结构体
 * - 使用Java类定义，包含Token统计字段
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go json标签 -> Jackson注解
 * - Go int64 -> Java Long
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenUsage {

  /**
   * 输入Token数量
   * 迁移对应关系: Go语言entity.TokenUsage.InputTokens (int64)
   * - 功能: 输入文本的Token数量
   * - 类型: Go的int64对应Java的Long
   * - 用途: 统计输入消耗
   */
  @JsonProperty("input_tokens")
  private Long inputTokens;

  /**
   * 输出Token数量
   * 迁移对应关系: Go语言entity.TokenUsage.OutputTokens (int64)
   * - 功能: 输出文本的Token数量
   * - 类型: Go的int64对应Java的Long
   * - 用途: 统计输出消耗
   */
  @JsonProperty("output_tokens")
  private Long outputTokens;

}
