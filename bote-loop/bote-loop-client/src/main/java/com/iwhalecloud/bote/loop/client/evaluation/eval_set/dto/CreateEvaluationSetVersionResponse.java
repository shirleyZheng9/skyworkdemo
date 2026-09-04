package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 创建评测集版本响应DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建评测集版本响应")
public class CreateEvaluationSetVersionResponse extends BaseResponse {

  @Schema(description = "版本ID")
  private Long id;
}
