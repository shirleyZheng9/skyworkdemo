package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评估集列表参数
 * 对应Go: ListEvaluationSetsParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluationSetsParam {

  private Long spaceId;
  private List<Long> evaluationSetIds;
  private String name;
  private List<String> creators;
  private String catalogItemId;
  private Integer pageNumber;
  private Integer pageSize;
  private String pageToken;
  private List<OrderBy> orderBys;
}
