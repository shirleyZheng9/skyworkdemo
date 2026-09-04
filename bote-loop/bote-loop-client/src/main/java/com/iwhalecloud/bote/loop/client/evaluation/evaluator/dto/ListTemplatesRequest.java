package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.EvaluatorTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表模板请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表模板请求")
public class ListTemplatesRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评估器类型")
  private EvaluatorTypeDTO evaluatorType;

  @Schema(description = "基础信息")
  private Base base;
}
