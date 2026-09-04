package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新评测集请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新评测集请求")
public class UpdateEvaluationSetRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集ID")
  private Long evaluationSetId;

  @Schema(description = "评测集名称")
  private String name;

  @Schema(description = "评测集描述")
  private String description;

  @Schema(description = "基础信息")
  private Base base;

  @Schema(description = "目录 ID")
  private Long catalogItemId;
}
