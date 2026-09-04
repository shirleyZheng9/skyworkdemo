package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 按源批量获取评估目标参数
 * 对应Go: BatchGetEvalTargetBySourceParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetEvalTargetBySourceReqParam {

  /**
   * 工作空间ID
   * 对应Go: SpaceID int64
   */
  private Long spaceId;

  /**
   * 源目标ID列表
   * 对应Go: SourceTargetID []string
   */
  private List<String> sourceTargetId;

  /**
   * 目标类型
   * 对应Go: TargetType entity.EvalTargetType
   */
  private EvalTargetType targetType;
}
