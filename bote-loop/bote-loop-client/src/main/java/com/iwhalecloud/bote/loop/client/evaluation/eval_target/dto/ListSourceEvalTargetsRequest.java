package com.iwhalecloud.bote.loop.client.evaluation.eval_target.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.eval_target.EvalTargetTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表源评测目标请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表源评测目标请求")
public class ListSourceEvalTargetsRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "目标类型")
  private EvalTargetTypeDTO targetType;

  @Schema(description = "名称")
  private String name;

  @Schema(description = "每页大小")
  private Integer pageSize;

  @Schema(description = "分页令牌")
  private String pageToken;

  @Schema(description = "基础信息")
  private Base base;
}
