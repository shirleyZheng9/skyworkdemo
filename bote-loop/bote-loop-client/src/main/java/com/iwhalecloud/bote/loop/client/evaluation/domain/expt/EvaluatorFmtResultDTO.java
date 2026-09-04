package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器格式化结果数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评估器格式化结果数据传输对象")
public class EvaluatorFmtResultDTO {

  @Schema(description = "名称")
  private String name;

  @Schema(description = "分数")
  private Double score;
}
