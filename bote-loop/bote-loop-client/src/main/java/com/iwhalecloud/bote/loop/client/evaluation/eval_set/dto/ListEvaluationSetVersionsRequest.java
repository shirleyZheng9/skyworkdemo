package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表评测集版本请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列表评测集版本请求")
public class ListEvaluationSetVersionsRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "评测集ID")
  private Long evaluationSetId;

  @Schema(description = "版本号模糊匹配")
  private String versionLike;

  @Schema(description = "页码")
  private Integer pageNumber;

  @Schema(description = "分页大小")
  private Integer pageSize;

  @Schema(description = "分页令牌")
  private String pageToken;

  @Schema(description = "基础信息")
  private Base base;
}
