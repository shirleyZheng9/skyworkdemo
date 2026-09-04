package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估集列表查询结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationSetListResult {

  private List<EvaluationSet> sets;
  private Long total;
  private String nextPageToken;
}
