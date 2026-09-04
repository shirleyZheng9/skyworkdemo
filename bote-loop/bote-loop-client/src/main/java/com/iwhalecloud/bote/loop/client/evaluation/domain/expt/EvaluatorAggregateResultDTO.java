package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器聚合结果数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评估器聚合结果数据传输对象")
public class EvaluatorAggregateResultDTO {

  @Schema(description = "评测器版本ID")
  private Long evaluatorVersionId;

  @Schema(description = "聚合器结果列表")
  private List<AggregatorResultDTO> aggregatorResults;

  @Schema(description = "名称")
  private String name;

  @Schema(description = "版本")
  private String version;
}
