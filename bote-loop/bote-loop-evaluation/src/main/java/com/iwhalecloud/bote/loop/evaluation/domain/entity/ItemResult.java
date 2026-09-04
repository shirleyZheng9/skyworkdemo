package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据项结果实体
 * 对应Go: entity.ItemResult
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemResult {

  /**
   * 数据项ID
   * 对应Go: ItemID int64
   */
  private Long itemId;

  /**
   * 行粒度实验结果详情
   * 对应Go: TurnResults []*TurnResult
   */
  private List<TurnResult> turnResults;

  /**
   * 系统信息
   * 对应Go: SystemInfo *ItemSystemInfo
   */
  private ItemSystemInfo systemInfo;

  /**
   * 数据项索引
   * 对应Go: ItemIndex *int64
   */
  private Long itemIndex;
}
