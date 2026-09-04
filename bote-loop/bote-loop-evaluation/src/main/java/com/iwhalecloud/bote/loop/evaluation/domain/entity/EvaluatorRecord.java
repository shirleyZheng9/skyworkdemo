package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器记录实体
 * 迁移对应关系: Go语言EvaluatorRecord
 * - 功能: 评估器记录数据结构
 * - 字段: id, spaceId, experimentId, experimentRunId, itemId, turnId, evaluatorVersionId, traceId, logId, evaluatorInputData, evaluatorOutputData, status, baseInfo, ext
 * <p>
 * Java实现说明:
 * - 对应Go的EvaluatorRecord结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现GetBaseInfo、SetBaseInfo、GetScore、GetReasoning、GetCorrected方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go *EvaluatorInputData -> Java EvaluatorInputData
 * - Go *EvaluatorOutputData -> Java EvaluatorOutputData
 * - Go EvaluatorRunStatus -> Java EvaluatorRunStatus
 * - Go *BaseInfo -> Java BaseInfo
 * - Go map[string]string -> Java Map<String, String>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluatorRecord {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("experiment_id")
  private Long experimentId;

  @JsonProperty("experiment_run_id")
  private Long experimentRunId;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("turn_id")
  private Long turnId;

  @JsonProperty("evaluator_version_id")
  private Long evaluatorVersionId;

  @JsonProperty("trace_id")
  private String traceId;

  @JsonProperty("log_id")
  private String logId;

  @JsonProperty("evaluator_input_data")
  private EvaluatorInputData evaluatorInputData;

  @JsonProperty("evaluator_output_data")
  private EvaluatorOutputData evaluatorOutputData;

  @JsonProperty("status")
  private EvaluatorRunStatus status;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;

  @JsonProperty("ext")
  private Map<String, String> ext;

  /**
   * 获取基础信息
   * 迁移对应关系: Go语言EvaluatorRecord.GetBaseInfo()
   */
  public BaseInfo getBaseInfo() {
    return this.baseInfo;
  }

  /**
   * 设置基础信息
   * 迁移对应关系: Go语言EvaluatorRecord.SetBaseInfo()
   */
  public void setBaseInfo(BaseInfo info) {
    this.baseInfo = info;
  }

  /**
   * 获取分数
   * 迁移对应关系: Go语言EvaluatorRecord.GetScore()
   */
  public Double getScore() {
    if (this.evaluatorOutputData == null || this.evaluatorOutputData.getEvaluatorResult() == null) {
      return null;
    }
    if (this.evaluatorOutputData.getEvaluatorResult().getCorrection() != null) {
      return this.evaluatorOutputData.getEvaluatorResult().getCorrection().getScore();
    }
    return this.evaluatorOutputData.getEvaluatorResult().getScore();
  }

  /**
   * 获取推理
   * 迁移对应关系: Go语言EvaluatorRecord.GetReasoning()
   */
  public String getReasoning() {
    if (this.evaluatorOutputData == null || this.evaluatorOutputData.getEvaluatorResult() == null) {
      return "";
    }
    if (this.evaluatorOutputData.getEvaluatorResult().getCorrection() != null) {
      return this.evaluatorOutputData.getEvaluatorResult().getCorrection().getExplain();
    }
    return this.evaluatorOutputData.getEvaluatorResult().getReasoning();
  }

  /**
   * 获取是否已纠正
   * 迁移对应关系: Go语言EvaluatorRecord.GetCorrected()
   */
  public Boolean getCorrected() {
    if (this.evaluatorOutputData == null || this.evaluatorOutputData.getEvaluatorResult() == null) {
      return false;
    }
    return this.evaluatorOutputData.getEvaluatorResult().getCorrection() != null;
  }
}
