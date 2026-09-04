package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验统计信息数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "实验统计信息数据传输对象")
public class ExptStatsInfoDTO {

  @Schema(description = "实验ID")
  private Long exptId;

  @Schema(description = "来源ID")
  private String sourceId;

  @Schema(description = "实验统计")
  private ExptStatisticsDTO exptStats;
}
