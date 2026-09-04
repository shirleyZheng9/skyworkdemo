package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮次结果数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "轮次结果数据传输对象")
public class TurnResultDTO {
  @Schema(description = "轮次ID")
  private Long turnId;
  @Schema(description = "实验结果列表")
  private List<ExperimentResultDTO> experimentResults;
  @Schema(description = "实验运行ID")
  private Long experimentRunId;
  @Schema(description = "轮次索引")
  private Long turnIndex;
}
