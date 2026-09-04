package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估目标记录实体
 * 迁移对应关系: Go语言EvalTargetRecord
 * - 功能: 评估目标记录数据结构
 * - 字段: id, spaceId, targetId, targetVersionId, experimentRunId, itemId, turnId, traceId, logId, evalTargetInputData, evalTargetOutputData, status, baseInfo
 * <p>
 * Java实现说明:
 * - 对应Go的EvalTargetRecord结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go *EvalTargetInputData -> Java EvalTargetInputData
 * - Go *EvalTargetOutputData -> Java EvalTargetOutputData
 * - Go *EvalTargetRunStatus -> Java EvalTargetRunStatus
 * - Go *BaseInfo -> Java BaseInfo
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalTargetRecord {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("target_id")
  private Long targetId;

  @JsonProperty("target_version_id")
  private Long targetVersionId;

  @JsonProperty("experiment_run_id")
  private Long experimentRunId;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("turn_id")
  private Long turnId;

  @JsonProperty("trace_id")
  private String traceId;

  @JsonProperty("log_id")
  private String logId;

  @JsonProperty("eval_target_input_data")
  private EvalTargetInputData evalTargetInputData;

  @JsonProperty("eval_target_output_data")
  private EvalTargetOutputData evalTargetOutputData;

  @JsonProperty("status")
  private EvalTargetRunStatus status;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;
}
