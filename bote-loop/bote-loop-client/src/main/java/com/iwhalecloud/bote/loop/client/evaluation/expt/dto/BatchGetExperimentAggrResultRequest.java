package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.common.SessionDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取实验聚合结果请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取实验聚合结果请求")
public class BatchGetExperimentAggrResultRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "实验ID列表")
  private List<Long> experimentIds;

  @Schema(description = "会话信息")
  private SessionDTO session;

  @Schema(description = "基础信息")
  private Base base;
}
