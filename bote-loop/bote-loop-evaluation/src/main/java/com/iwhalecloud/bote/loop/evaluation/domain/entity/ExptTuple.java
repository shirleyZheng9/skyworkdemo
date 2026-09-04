package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验元组实体
 * 迁移对应关系: Go语言ExptTuple
 * - 功能: 实验元组数据结构
 * - 字段: target, evalSet, evaluators
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTuple结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *EvalTarget -> Java EvalTarget
 * - Go *EvaluationSet -> Java EvaluationSet
 * - Go []*Evaluator -> Java List<Evaluator>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTuple {
  @JsonProperty("target")
  private EvalTarget target;

  @JsonProperty("eval_set")
  private EvaluationSet evalSet;

  @JsonProperty("evaluators")
  private List<Evaluator> evaluators;
}
