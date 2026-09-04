package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 得分分布数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "得分分布数据传输对象")
public class ScoreDistributionDTO {

  @Schema(description = "得分分布项列表")
  private List<ScoreDistributionItemDTO> scoreDistributionItems;
}
