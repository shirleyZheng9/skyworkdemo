package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验聚合结果实体
 * 迁移对应关系: Go语言ExptAggregateResult
 * - 功能: 实验聚合结果数据结构
 * - 字段: experimentId, evaluatorResults, status
 * <p>
 * Java实现说明:
 * - 对应Go的ExptAggregateResult结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go map[int64]*EvaluatorAggregateResult -> Java Map<Long, EvaluatorAggregateResult>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptAggregateResult {
  @JsonProperty("experiment_id")
  private Long experimentId;

  @JsonProperty("evaluator_results")
  private Map<Long, EvaluatorAggregateResult> evaluatorResults;

  @JsonProperty("status")
  private Integer status;
}
