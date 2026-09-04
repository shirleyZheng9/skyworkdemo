package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 源目标版本列表查询参数
 * 对应Go: ListSourceVersionParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListSourceVersionParam {

  /**
   * 工作空间ID
   * 对应Go: SpaceID *int64
   */
  private Long spaceId;

  /**
   * 分页大小
   * 对应Go: PageSize int32
   */
  private Integer pageSize;

  /**
   * 游标
   * 对应Go: Cursor string
   */
  private String cursor;

  /**
   * 源目标ID
   * 对应Go: SourceTargetID string
   */
  private String sourceTargetId;

  /**
   * 目标类型
   * 对应Go: TargetType entity.EvalTargetType
   */
  private EvalTargetType targetType;
}
