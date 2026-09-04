package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器实体
 * 迁移对应关系: Go语言Evaluator
 * - 功能: 评估器数据结构
 * - 字段: id, spaceId, name, description, draftSubmitted, evaluatorType, latestVersion, baseInfo, promptEvaluatorVersion
 * <p>
 * Java实现说明:
 * - 对应Go的Evaluator结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现GetEvaluatorVersion、SetEvaluatorVersion方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go bool -> Java Boolean
 * - Go *BaseInfo -> Java BaseInfo
 * - Go *PromptEvaluatorVersion -> Java PromptEvaluatorVersion
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluator {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("name")
  private String name;

  @JsonProperty("description")
  private String description;

  @JsonProperty("draft_submitted")
  private Boolean draftSubmitted;

  @JsonProperty("evaluator_type")
  private EvaluatorType evaluatorType;

  @JsonProperty("latest_version")
  private String latestVersion;

  @JsonProperty("base_info")
  private BaseInfo baseInfo;

  @JsonProperty("prompt_evaluator_version")
  private PromptEvaluatorVersion promptEvaluatorVersion;

  @JsonProperty("catalog_item_id")
  private Long catalogItemId;

  /**
   * 获取评估器版本
   * 迁移对应关系: Go语言Evaluator.GetEvaluatorVersion()
   */
  public IEvaluatorVersion getEvaluatorVersion() {
    switch (this.evaluatorType) {
      case PROMPT:
        return this.promptEvaluatorVersion;
      default:
        return null;
    }
  }

  /**
   * 设置评估器版本
   * 迁移对应关系: Go语言Evaluator.SetEvaluatorVersion()
   */
  public void setEvaluatorVersion(Evaluator version) {
    switch (this.evaluatorType) {
      case PROMPT:
        this.promptEvaluatorVersion = version.promptEvaluatorVersion;
        break;
      default:
        break;
    }
  }
}
