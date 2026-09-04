package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除评测器请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "删除评测器请求")
public class DeleteEvaluatorRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测器ID")
  private Long evaluatorId;

  @Schema(description = "基础信息")
  private Base base;
}
