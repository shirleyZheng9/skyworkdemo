package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 源目标列表结果
 * 对应Go: ListSourceResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListSourceResult {

  /**
   * 评估目标列表
   * 对应Go: Targets []*entity.EvalTarget
   */
  private List<EvalTarget> evalTargets;

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
