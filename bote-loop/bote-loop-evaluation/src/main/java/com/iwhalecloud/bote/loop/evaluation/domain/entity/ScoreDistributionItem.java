package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分数分布项实体
 * 迁移对应关系: Go语言ScoreDistributionItem
 * - 功能: 分数分布项数据结构
 * - 字段: score, count, percentage
 * <p>
 * Java实现说明:
 * - 对应Go的ScoreDistributionItem结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go string -> Java String
 * - Go int64 -> Java Long
 * - Go float64 -> Java Double
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreDistributionItem {
  @JsonProperty("score")
  private String score;

  @JsonProperty("count")
  private Long count;

  @JsonProperty("percentage")
  private Double percentage;
}
