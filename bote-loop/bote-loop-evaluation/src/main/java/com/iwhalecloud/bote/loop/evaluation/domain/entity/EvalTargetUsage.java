package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估目标使用情况实体
 * 迁移对应关系: Go语言EvalTargetUsage
 * - 功能: 评估目标使用情况数据结构
 * - 字段: inputTokens, outputTokens
 * <p>
 * Java实现说明:
 * - 对应Go的EvalTargetUsage结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalTargetUsage {
  @JsonProperty("input_tokens")
  private Long inputTokens;

  @JsonProperty("output_tokens")
  private Long outputTokens;
}
