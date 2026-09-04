package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评估器请求
 * 对应Go: ListEvaluatorRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluatorRequest {

  private Long spaceId;
  private String searchName;
  private List<Long> creatorIds;
  private List<EvaluatorType> evaluatorType;
  private Integer pageSize;
  private Integer pageNum;
  private List<OrderBy> orderBys;
  private Boolean withVersion;
  private String catalogItemId;
}
