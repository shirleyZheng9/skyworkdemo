package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评估集数据项列表结果
 * 对应Go: ListEvaluationSetItemsResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluationSetItemsResult {

  /**
   * 数据项列表
   */
  private List<EvaluationSetItem> items;

  /**
   * 总数
   */
  private Long total;

  /**
   * 下一页令牌
   */
  private String nextPageToken;
}
