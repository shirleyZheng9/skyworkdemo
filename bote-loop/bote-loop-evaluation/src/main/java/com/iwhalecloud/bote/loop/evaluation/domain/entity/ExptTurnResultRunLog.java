package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验轮次结果运行日志实体
 * 迁移对应关系: Go语言ExptTurnResultRunLog
 * - 功能: 实验轮次结果运行日志数据结构
 * - 字段: id, spaceId, exptId, exptRunId, itemId, turnId, status, traceId, logId, targetResultId, evaluatorResultIds, errMsg
 * <p>
 * Java实现说明:
 * - 对应Go的ExptTurnResultRunLog结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go TurnRunState -> Java TurnRunState
 * - Go string -> Java String
 * - Go *EvaluatorResults -> Java EvaluatorResults
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptTurnResultRunLog {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("expt_id")
  private Long exptId;

  @JsonProperty("expt_run_id")
  private Long exptRunId;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("turn_id")
  private Long turnId;

  @JsonProperty("status")
  private TurnRunState status;

  @JsonProperty("trace_id")
  private Long traceId;

  @JsonProperty("log_id")
  private String logId;

  @JsonProperty("target_result_id")
  private Long targetResultId;

  @JsonProperty("evaluator_result_ids")
  private EvaluatorResults evaluatorResultIds;

  @JsonProperty("err_msg")
  private String errMsg;
}
