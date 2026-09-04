package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验项结果实体
 * 迁移对应关系: Go语言ExptItemResult
 * - 功能: 实验项结果数据结构
 * - 字段: id, spaceId, exptId, exptRunId, itemId, status, errMsg, itemIdx, logId
 * <p>
 * Java实现说明:
 * - 对应Go的ExptItemResult结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go ItemRunState -> Java ItemRunState
 * - Go string -> Java String
 * - Go int32 -> Java Integer
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptItemResult {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("expt_id")
  private Long exptId;

  @JsonProperty("expt_run_id")
  private Long exptRunId;

  @JsonProperty("item_id")
  private Long itemId;

  @JsonProperty("status")
  private ItemRunState status;

  @JsonProperty("err_msg")
  private String errMsg;

  @JsonProperty("item_idx")
  private Integer itemIdx;

  @JsonProperty("log_id")
  private String logId;
}
