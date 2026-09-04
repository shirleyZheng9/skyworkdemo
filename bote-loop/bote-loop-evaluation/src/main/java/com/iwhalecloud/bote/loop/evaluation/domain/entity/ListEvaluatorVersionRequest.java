package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评估器版本请求
 * 对应Go: ListEvaluatorVersionRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluatorVersionRequest {

  private Long spaceId;
  private Long evaluatorId;
  private List<String> queryVersions;
  private Integer pageSize;
  private Integer pageNum;
  private List<OrderBy> orderBys;
}
