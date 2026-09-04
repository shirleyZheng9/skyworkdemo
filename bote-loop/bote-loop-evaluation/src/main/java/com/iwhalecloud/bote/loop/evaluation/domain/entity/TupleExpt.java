package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 元组实验实体
 * 迁移对应关系: Go语言TupleExpt
 * - 功能: 元组实验数据结构
 * - 字段: expt, target, evalSet, evaluators
 * <p>
 * Java实现说明:
 * - 对应Go的TupleExpt结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *Experiment -> Java Experiment
 * - Go *EvalTarget -> Java EvalTarget
 * - Go *EvaluationSet -> Java EvaluationSet
 * - Go []*Evaluator -> Java List<Evaluator>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TupleExpt {
  @JsonProperty("expt")
  private Experiment expt;

  @JsonProperty("target")
  private EvalTarget target;

  @JsonProperty("eval_set")
  private EvaluationSet evalSet;

  @JsonProperty("evaluators")
  private List<Evaluator> evaluators;
}
