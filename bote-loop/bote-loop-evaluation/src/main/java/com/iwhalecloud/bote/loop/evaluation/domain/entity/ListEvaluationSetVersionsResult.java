package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评估集版本列表结果
 * 对应Go: ListEvaluationSetVersionsResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluationSetVersionsResult {

  private List<EvaluationSetVersion> versions;
  private Long total;
  private String nextCursor;
}
