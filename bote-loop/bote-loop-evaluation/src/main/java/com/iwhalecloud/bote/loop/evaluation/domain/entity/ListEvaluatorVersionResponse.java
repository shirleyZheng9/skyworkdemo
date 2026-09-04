package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询评估器版本响应
 * 对应Go: ListEvaluatorVersionResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluatorVersionResponse {

  private List<Evaluator> evaluatorVersions;
  private Long total;
}
