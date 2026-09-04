package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列出评估器响应
 * 对应Go: ListEvaluatorResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluatorResponse {

  /**
   * 总数量
   * 对应Go: TotalCount int64
   */
  private Long totalCount;

  /**
   * 评估器列表
   * 对应Go: Evaluators []*entity.Evaluator
   */
  private List<Evaluator> evaluators;
}
