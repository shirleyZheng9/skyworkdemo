package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 克隆实验响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "克隆实验响应")
public class CloneExperimentResponse {

  @Schema(description = "实验信息")
  private ExperimentDTO experiment;

  @Schema(description = "基础信息")
  private Base base;
}
