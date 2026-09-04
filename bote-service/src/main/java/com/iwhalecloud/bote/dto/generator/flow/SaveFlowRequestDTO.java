package com.iwhalecloud.bote.dto.generator.flow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI 保存工作流请求
 *
 * @author bianjp
 * @since 2025-05-26
 */
@Getter
@Setter
@ToString
@Schema(description = "AI 保存工作流请求")
public class SaveFlowRequestDTO {
  @Schema(description = "租户 ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long tenantId;
  @Schema(description = "工作流 ID（为空时表示新增，不为空表示修改）")
  private Long flowId;
  @Schema(description = "工作流类型")
  private String flowType;
  @Schema(description = "工作流名称")
  private String flowName;
  @Schema(description = "功能描述", requiredMode = Schema.RequiredMode.REQUIRED)
  private String prompt;
}
