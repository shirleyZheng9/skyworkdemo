package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估目标创建结果
 * 对应Go: CreateEvalTargetResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvalTargetCreateResult {

  /**
   * 目标ID
   * 对应Go: ID int64
   */
  private Long id;

  /**
   * 版本ID
   * 对应Go: VersionID int64
   */
  private Long versionId;
}
