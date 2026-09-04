package com.iwhalecloud.bote.loop.client.evaluation.evaluator.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.evaluator.ToolDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取默认提示词评测器工具响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "获取默认提示词评测器工具响应")
public class GetDefaultPromptEvaluatorToolsResponse {

  @Schema(description = "工具列表")
  private List<ToolDTO> tools;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
