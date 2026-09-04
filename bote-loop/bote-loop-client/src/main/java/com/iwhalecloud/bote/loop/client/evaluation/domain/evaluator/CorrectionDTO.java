package com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修正数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "修正数据传输对象")
public class CorrectionDTO {

  @Schema(description = "分数")
  private Double score;

  @Schema(description = "说明")
  private String explain;

  @Schema(description = "更新人")
  private String updatedBy;
}
