package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验计算统计实体
 * 迁移对应关系: Go语言ExptCalculateStats
 * - 功能: 实验计算统计数据结构
 * - 字段: pendingItemCnt, failItemCnt, successItemCnt, processingItemCnt, terminatedItemCnt, incompleteTurnIDs
 * <p>
 * Java实现说明:
 * - 对应Go的ExptCalculateStats结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int -> Java int
 * - Go []*ItemTurnID -> Java List<ItemTurnID>
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptCalculateStats {
  @JsonProperty("pending_item_cnt")
  private int pendingItemCnt;

  @JsonProperty("fail_item_cnt")
  private int failItemCnt;

  @JsonProperty("success_item_cnt")
  private int successItemCnt;

  @JsonProperty("processing_item_cnt")
  private int processingItemCnt;

  @JsonProperty("terminated_item_cnt")
  private int terminatedItemCnt;

  @JsonProperty("incomplete_turn_ids")
  private List<ItemTurnID> incompleteTurnIds;
}
