package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExperimentDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表实验响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表实验响应")
public class ListExperimentsResponse {

  @Schema(description = "实验列表")
  private List<ExperimentDTO> experiments;

  @Schema(description = "总数")
  private Long total;

  @Schema(description = "基础信息")
  private Base base;
}
