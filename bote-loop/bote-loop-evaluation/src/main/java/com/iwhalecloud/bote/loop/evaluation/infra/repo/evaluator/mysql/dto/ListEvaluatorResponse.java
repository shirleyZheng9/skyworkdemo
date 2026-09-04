package com.iwhalecloud.bote.loop.evaluation.infra.repo.evaluator.mysql.dto;

import com.iwhalecloud.bote.entity.loop.evaluation.EvaluatorEntity;
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
  private List<EvaluatorEntity> evaluators;
}
