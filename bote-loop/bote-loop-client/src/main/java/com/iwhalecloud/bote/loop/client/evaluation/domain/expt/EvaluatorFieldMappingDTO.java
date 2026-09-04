package com.iwhalecloud.bote.loop.client.evaluation.domain.expt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评估器字段映射数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "评估器字段映射数据传输对象")
public class EvaluatorFieldMappingDTO {

  @Schema(description = "评测器版本ID")
  private Long evaluatorVersionId;

  @Schema(description = "从评测集映射的字段列表")
  private List<FieldMappingDTO> fromEvalSet;

  @Schema(description = "从目标映射的字段列表")
  private List<FieldMappingDTO> fromTarget;

  @Schema(description = "通过得分")
  private Double passScore;
}
