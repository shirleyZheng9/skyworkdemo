package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评估集数据项列表参数
 * 对应Go: ListEvaluationSetItemsParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluationSetItemsParam {

  private Long spaceId;
  private Long evaluationSetId;
  private Long versionId;
  private Integer pageNumber;
  private Integer pageSize;
  private String pageToken;
  private List<OrderBy> orderBys;
  private List<Long> itemIdsNotIn;
}
