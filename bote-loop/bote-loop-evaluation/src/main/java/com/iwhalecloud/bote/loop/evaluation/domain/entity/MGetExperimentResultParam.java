package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取实验结果参数
 * 对应Go: MGetExperimentResultParam
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MGetExperimentResultParam {

  /**
   * 工作空间ID
   * 对应Go: SpaceID int64
   */
  private Long spaceId;

  /**
   * 实验ID列表
   * 对应Go: ExptIDs []int64
   */
  private List<Long> exptIds;

  /**
   * 基线实验ID
   * 对应Go: BaseExptID int64
   */
  private Long baseExptId;

  /**
   * 分页信息
   * 对应Go: Page *entity.Page
   */
  private Page page;

  /**
   * 是否使用加速器
   * 对应Go: UseAccelerator bool
   */
  private Boolean useAccelerator;

  /**
   * 过滤器
   * 对应Go: Filters map[int64]*entity.ExptTurnResultFilter
   */
  private Map<Long, ExptTurnResultFilter> filters;

  /**
   * 加速器过滤器
   * 对应Go: FilterAccelerators map[int64]*entity.ExptTurnResultFilterAccelerator
   */
  private Map<Long, ExptTurnResultFilterAccelerator> filterAccelerators;
  @Schema(description = "数据项id列表")
  private List<Long> itemIds;
}
