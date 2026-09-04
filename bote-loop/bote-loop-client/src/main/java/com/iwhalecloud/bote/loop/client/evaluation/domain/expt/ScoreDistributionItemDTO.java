package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 得分分布项数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "得分分布项数据传输对象")
public class ScoreDistributionItemDTO {

  @Schema(description = "分数")
  private String score;

  @Schema(description = "数量")
  private Long count;

  @Schema(description = "百分比")
  private Double percentage;
}
