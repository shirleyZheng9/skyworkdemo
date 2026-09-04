package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取评估集数据项参数
 * 对应Go: BatchGetEvaluationSetItemsParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetEvaluationSetItemsParam {

  private Long spaceId;
  private Long evaluationSetId;
  private List<Long> itemIds;
  private Long versionId;
  private Integer pageNumber;
  private Integer pageSize;
  private String pageToken;
  private List<OrderBy> orderBys;
}
