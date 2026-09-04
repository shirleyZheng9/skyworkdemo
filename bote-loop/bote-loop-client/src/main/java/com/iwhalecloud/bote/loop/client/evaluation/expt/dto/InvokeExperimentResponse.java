package com.iwhalecloud.bote.loop.client.evaluation.expt.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.data.domain.dataset.ItemErrorGroupDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调用实验响应
 * 对应Go: expt.InvokeExperimentResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvokeExperimentResponse {

  /**
   * 已添加的项目映射，key: item 在 items 中的索引
   * 对应Go: AddedItems map[int64]int64
   */
  @Schema(description = "已添加的项目映射")
  private Map<Long, Long> addedItems;

  /**
   * 错误信息列表
   * 对应Go: Errors []*dataset.ItemErrorGroup
   */
  @Schema(description = "错误信息列表")
  private List<ItemErrorGroupDTO> errors;

  /**
   * 基础响应信息
   * 对应Go: BaseResp *base.BaseResp
   */
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
