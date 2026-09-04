package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 得分过滤器实体
 * 迁移对应关系: Go语言ScoreFilter
 * - 功能: 得分过滤条件
 * - 字段: score, operator, evaluatorVersionId
 * <p>
 * Java实现说明:
 * - 对应Go的ScoreFilter结构体
 * - 使用Lombok注解简化代码
 * - JSON序列化使用camelCase命名（研发规范2）
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go float64 -> Java Double
 * - Go string -> Java String
 * - Go int64 -> Java Long
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoreFilter {
  private Double score;

  private String operator;

  private Long evaluatorVersionId;
}
