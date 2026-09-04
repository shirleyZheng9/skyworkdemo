package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.EvalTargetType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 根据来源批量获取评估目标参数
 * 对应Go: BatchGetEvalTargetBySourceParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetEvalTargetBySourceParam {

  /**
   * 空间ID
   * 对应Go: SpaceID int64
   */
  private Long spaceId;

  /**
   * 来源目标ID列表
   * 对应Go: SourceTargetID []string
   */
  private List<String> sourceTargetId;

  /**
   * 目标类型
   * 对应Go: TargetType entity.EvalTargetType
   */
  private EvalTargetType targetType;
}
