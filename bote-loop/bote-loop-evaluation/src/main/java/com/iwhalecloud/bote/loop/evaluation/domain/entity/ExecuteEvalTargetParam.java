package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 执行评估目标参数
 * 对应Go: ExecuteEvalTargetParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteEvalTargetParam {

  /**
   * 目标ID
   * 对应Go: TargetID int64
   */
  private Long targetId;

  /**
   * 版本ID
   * 对应Go: VersionID int64
   */
  private Long versionId;

  /**
   * 源目标ID
   * 对应Go: SourceTargetID string
   */
  private String sourceTargetId;

  /**
   * 源目标版本
   * 对应Go: SourceTargetVersion string
   */
  private String sourceTargetVersion;

  /**
   * 输入数据
   * 对应Go: Input *entity.EvalTargetInputData
   */
  private EvalTargetInputData input;

  /**
   * 目标类型
   * 对应Go: TargetType entity.EvalTargetType
   */
  private EvalTargetType targetType;
}
