package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评估集版本列表参数
 * 对应Go: ListEvaluationSetVersionsParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluationSetVersionsParam {

  private Long spaceId;
  private Long evaluationSetId;
  private String pageToken;
  private Integer pageSize;
  private Integer pageNumber;
  private String versionLike;
}
