package com.iwhalecloud.bote.loop.client.evaluation.eval_set.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取评测集版本请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取评测集版本请求")
public class BatchGetEvaluationSetVersionsRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "版本ID列表")
  private List<Long> versionIds;

  @Schema(description = "是否包含已删除")
  private Boolean deletedAt;

  @Schema(description = "基础信息")
  private Base base;
}
