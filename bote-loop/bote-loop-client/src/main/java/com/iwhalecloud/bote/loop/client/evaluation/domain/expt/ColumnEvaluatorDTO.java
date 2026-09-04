package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列评估器数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列评估器数据传输对象")
public class ColumnEvaluatorDTO {

  @Schema(description = "评测器版本ID")
  private Long evaluatorVersionId;

  @Schema(description = "评测器ID")
  private Long evaluatorId;

  @Schema(description = "评测器类型")
  private EvaluatorTypeDTO evaluatorType;

  @Schema(description = "名称")
  private String name;

  @Schema(description = "版本")
  private String version;

  @Schema(description = "描述")
  private String description;
}
