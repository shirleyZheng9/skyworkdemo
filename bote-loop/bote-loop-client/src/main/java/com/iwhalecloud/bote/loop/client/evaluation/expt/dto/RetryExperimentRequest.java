package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptRetryModeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重试实验请求
 * 对应Go: expt.RetryExperimentRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "重试实验请求")
public class RetryExperimentRequest {
  @Schema(description = "重试模式")
  private ExptRetryModeDTO retryMode;
  @Schema(description = "工作空间ID")
  private Long workspaceId;
  @Schema(description = "实验ID")
  private Long exptId;
  @Schema(description = "实验ID列表")
  private List<Long> exptIds;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "项目ID列表")
  private List<Long> itemIds;
  @Schema(description = "重跑轮数")
  private Integer turn;
  @Schema(description = "扩展信息")
  private Map<String, String> ext;
  @Schema(description = "基础信息")
  private Base base;
}
