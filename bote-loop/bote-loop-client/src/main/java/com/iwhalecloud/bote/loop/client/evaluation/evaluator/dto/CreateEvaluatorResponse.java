package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评测器响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建评测器响应")
public class CreateEvaluatorResponse {

  @Schema(description = "评测器ID")
  private Long evaluatorId;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
