package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 提交实验响应
 * 对应Go: expt.SubmitExperimentResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitExperimentResponse {

  /**
   * 实验信息
   * 对应Go: Experiment *expt.Experiment
   */
  @Schema(description = "实验信息")
  private ExperimentDTO experiment;

  /**
   * 运行ID
   * 对应Go: RunID *int64
   */
  @Schema(description = "运行ID")
  private Long runId;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
