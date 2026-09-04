package com.iwhalecloud.bote.loop.evaluation.domain.repo.dto;

import com.iwhalecloud.bote.loop.evaluation.domain.entity.Evaluator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列出评估器版本响应
 * 对应Go: ListEvaluatorVersionResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListEvaluatorVersionResponse {

  /**
   * 总数量
   * 对应Go: TotalCount int64
   */
  private Long totalCount;

  /**
   * 版本列表
   * 对应Go: Versions []*entity.Evaluator
   */
  private List<Evaluator> versions;
}
