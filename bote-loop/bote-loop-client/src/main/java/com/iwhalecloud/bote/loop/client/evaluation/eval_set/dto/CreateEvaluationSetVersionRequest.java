package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建评测集版本请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建评测集版本请求")
public class CreateEvaluationSetVersionRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集ID")
  private Long evaluationSetId;

  @Schema(description = "版本号")
  private String version;

  @Schema(description = "版本描述")
  private String desc;

  @Schema(description = "基础信息")
  private Base base;
}
