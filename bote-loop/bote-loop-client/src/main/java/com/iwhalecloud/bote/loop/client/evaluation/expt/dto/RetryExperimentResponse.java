package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重试实验响应
 * 对应Go: expt.RetryExperimentResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RetryExperimentResponse {

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
