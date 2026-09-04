package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聚合数据数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "聚合数据数据传输对象")
public class AggregateDataDTO {

  @Schema(description = "数据类型")
  private DataTypeDTO dataType;

  @Schema(description = "值")
  private Double value;

  @Schema(description = "分数分布")
  private ScoreDistributionDTO scoreDistribution;
}
