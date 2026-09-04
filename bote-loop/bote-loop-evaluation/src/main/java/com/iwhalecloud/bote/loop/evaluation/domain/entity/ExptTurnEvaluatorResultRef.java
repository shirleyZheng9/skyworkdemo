package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次评估器结果引用实体
 * 迁移对应关系: Go语言ExptTurnEvaluatorResultRef
 * - 功能: 实验轮次评估器结果引用数据结构
 * - 字段: id, spaceId, exptTurnResultId, evaluatorVersionId, evaluatorResultId, exptId
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnEvaluatorResultRef结构体
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
public class ExptTurnEvaluatorResultRef {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("expt_turn_result_id")
  private Long exptTurnResultId;

  @JsonProperty("evaluator_version_id")
  private Long evaluatorVersionId;

  @JsonProperty("evaluator_result_id")
  private Long evaluatorResultId;

  @JsonProperty("expt_id")
  private Long exptId;
}
