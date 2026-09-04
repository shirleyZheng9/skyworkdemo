package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验统计实体
 * 迁移对应关系: Go语言ExptStats
 * - 功能: 实验统计数据结构
 * - 字段: id, spaceId, exptId, pendingItemCnt, successItemCnt, failItemCnt, processingItemCnt, terminatedItemCnt, creditCost, inputTokenCost, outputTokenCost, createdAt, updatedAt
 * <p>
 * Java实现说明:
 * - 对应Go的ExptStats结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go float64 -> Java Double
 * - Go time.Time -> Java Date
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptStats {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("expt_id")
  private Long exptId;

  @JsonProperty("pending_item_cnt")
  private Integer pendingItemCnt;

  @JsonProperty("success_item_cnt")
  private Integer successItemCnt;

  @JsonProperty("fail_item_cnt")
  private Integer failItemCnt;

  @JsonProperty("processing_item_cnt")
  private Integer processingItemCnt;

  @JsonProperty("terminated_item_cnt")
  private Integer terminatedItemCnt;

  @JsonProperty("credit_cost")
  private Double creditCost;

  @JsonProperty("input_token_cost")
  private Long inputTokenCost;

  @JsonProperty("output_token_cost")
  private Long outputTokenCost;

  @JsonProperty("created_at")
  private Date createdAt;

  @JsonProperty("updated_at")
  private Date updatedAt;
}
