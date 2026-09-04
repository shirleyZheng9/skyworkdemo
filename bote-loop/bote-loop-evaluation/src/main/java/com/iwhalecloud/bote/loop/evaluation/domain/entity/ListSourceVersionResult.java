package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 源目标版本列表结果
 * 对应Go: ListSourceVersionResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListSourceVersionResult {

  /**
   * 评估目标版本列表
   * 对应Go: Versions []*entity.EvalTargetVersion
   */
  private List<EvalTargetVersion> versions;

  /**
   * 下一页游标
   * 对应Go: NextCursor string
   */
  private String nextCursor;

  /**
   * 是否有更多
   * 对应Go: HasMore bool
   */
  private Boolean hasMore;

  /**
   * 总数
   * 对应Go: Total int64
   */
  private Long total;
}
