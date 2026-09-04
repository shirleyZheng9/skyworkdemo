package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetDTO;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_set.EvaluationSetVersionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 版本化评估集DTO
 * 对应Go: VersionedEvaluationSet
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "版本化评估集")
public class VersionedEvaluationSetDTO {

  @Schema(description = "评估集信息")
  private EvaluationSetDTO evaluationSet;

  @Schema(description = "评估集版本信息")
  private EvaluationSetVersionDTO version;
}
