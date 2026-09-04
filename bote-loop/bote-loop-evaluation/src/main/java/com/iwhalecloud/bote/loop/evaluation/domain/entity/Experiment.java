package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验实体
 * 迁移对应关系: Go语言Experiment
 * - 功能: 实验数据结构
 * - 字段: id, spaceId, createdBy, name, description, evalSetVersionId, evalSetId, targetType, targetVersionId, targetId, evaluatorVersionRef, evalConf, target, evalSet, evaluators, status, statusMessage, latestRunId, creditCost, startAt, endAt, exptType, maxAliveTime, sourceType, sourceId, stats, aggregateResult
 * <p>
 * Java实现说明:
 * - 对应Go的Experiment结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现ToEvaluatorRefDO方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go EvalTargetType -> Java EvalTargetType
 * - Go []*ExptEvaluatorVersionRef -> Java List<ExptEvaluatorVersionRef>
 * - Go *EvaluationConfiguration -> Java EvaluationConfiguration
 * - Go *EvalTarget -> Java EvalTarget
 * - Go *EvaluationSet -> Java EvaluationSet
 * - Go []*Evaluator -> Java List<Evaluator>
 * - Go ExptStatus -> Java ExptStatus
 * - Go CreditCost -> Java CreditCost
 * - Go *time.Time -> Java Date
 * - Go ExptType -> Java ExptType
 * - Go SourceType -> Java SourceType
 * - Go *ExptStats -> Java ExptStats
 * - Go *ExptAggregateResult -> Java ExptAggregateResult
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Experiment {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("eval_set_version_id")
  private Long evalSetVersionId;

  @JsonProperty("eval_set_id")
  private Long evalSetId;

  @JsonProperty("target_type")
  private EvalTargetType targetType;

  @JsonProperty("target_version_id")
  private Long targetVersionId;

  @JsonProperty("target_id")
  private Long targetId;

  @JsonProperty("evaluator_version_ref")
  private List<ExptEvaluatorVersionRef> evaluatorVersionRef;

  @JsonProperty("eval_conf")
  private EvaluationConfiguration evalConf;

  @JsonProperty("target")
  private EvalTarget target;

  @JsonProperty("eval_set")
  private EvaluationSet evalSet;

  @JsonProperty("evaluators")
  private List<Evaluator> evaluators;

  @JsonProperty("status")
  private ExptStatus status;

  @JsonProperty("status_message")
  private String statusMessage;

  @JsonProperty("latest_run_id")
  private Long latestRunId;

  @JsonProperty("credit_cost")
  private CreditCost creditCost;

  @JsonProperty("start_at")
  private Date startAt;

  @JsonProperty("end_at")
  private Date endAt;

  @JsonProperty("expt_type")
  private ExptType exptType;

  @JsonProperty("max_alive_time")
  private Long maxAliveTime;

  @JsonProperty("source_type")
  private SourceType sourceType;

  @JsonProperty("source_id")
  private String sourceId;

  @JsonProperty("catalog_item_id")
  private Long catalogItemId;

  @JsonProperty("stats")
  private ExptStats stats;

  @JsonProperty("aggregate_result")
  private ExptAggregateResult aggregateResult;

  /**
   * 转换为评估器引用DO
   * 迁移对应关系: Go语言Experiment.ToEvaluatorRefDO()
   */
  public List<ExptEvaluatorRef> toEvaluatorRefDO() {
    if (this.evaluatorVersionRef == null) {
      return null;
    }

    List<ExptEvaluatorRef> refs = new java.util.ArrayList<>();
    for (ExptEvaluatorVersionRef evr : this.evaluatorVersionRef) {
      refs.add(ExptEvaluatorRef.builder()
        .spaceId(this.spaceId)
        .exptId(this.id)
        .evaluatorId(evr.getEvaluatorId())
        .evaluatorVersionId(evr.getEvaluatorVersionId())
        .build());
    }
    return refs;
  }
}
