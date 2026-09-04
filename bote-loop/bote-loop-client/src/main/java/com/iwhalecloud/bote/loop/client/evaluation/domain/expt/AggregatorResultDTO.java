package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聚合器结果数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "聚合器结果数据传输对象")
public class AggregatorResultDTO {

  @Schema(description = "聚合器类型")
  private AggregatorTypeDTO aggregatorType;

  @Schema(description = "聚合数据")
  private AggregateDataDTO data;
}
