package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.evaluation.domain.expt.ExptStatsInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列出实验统计响应
 * 对应Go: expt.ListExperimentStatsResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "列出实验统计响应")
public class ListExperimentStatsResponse {

  /**
   * 实验统计信息列表
   * 对应Go: ExptStatsInfos []*expt.ExptStatsInfo
   */
  @Schema(description = "实验统计信息列表")
  private List<ExptStatsInfoDTO> exptStatsInfos;

  /**
   * 总数
   * 对应Go: Total *int32
   */
  @Schema(description = "总数")
  private Long total;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
