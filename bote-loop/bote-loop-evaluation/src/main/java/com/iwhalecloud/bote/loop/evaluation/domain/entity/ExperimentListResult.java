package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验列表结果
 * 对应Go: ListExperimentResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperimentListResult {

  /**
   * 实验列表
   * 对应Go: Experiments []*entity.Experiment
   */
  private List<Experiment> experiments;

  /**
   * 总数
   * 对应Go: Total int64
   */
  private Long total;
}
