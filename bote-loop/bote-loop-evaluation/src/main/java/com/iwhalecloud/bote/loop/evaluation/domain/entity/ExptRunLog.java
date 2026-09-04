package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验运行日志实体
 * 迁移对应关系: Go语言ExptRunLog
 * - 功能: 实验运行日志数据结构
 * - 字段: id, spaceId, createdBy, exptId, exptRunId, itemIds, mode, status, pendingCnt, successCnt, failCnt, creditCost, tokenCost, statusMessage, processingCnt, terminatedCnt, createdAt, updatedAt
 * <p>
 * Java实现说明:
 * - 对应Go的ExptRunLog结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go []byte -> Java byte[]
 * - Go int32 -> Java Integer
 * - Go float64 -> Java Double
 * - Go time.Time -> Java LocalDateTime
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptRunLog {
  @JsonProperty("id")
  private Long id;

  @JsonProperty("space_id")
  private Long spaceId;

  @JsonProperty("created_by")
  private String createdBy;

  @JsonProperty("expt_id")
  private Long exptId;

  @JsonProperty("expt_run_id")
  private Long exptRunId;

  @JsonProperty("item_ids")
  private String itemIds;

  @JsonProperty("mode")
  private Integer mode;

  @JsonProperty("status")
  private Long status;

  @JsonProperty("pending_cnt")
  private Integer pendingCnt;

  @JsonProperty("success_cnt")
  private Integer successCnt;

  @JsonProperty("fail_cnt")
  private Integer failCnt;

  @JsonProperty("credit_cost")
  private Double creditCost;

  @JsonProperty("token_cost")
  private Long tokenCost;

  @JsonProperty("status_message")
  private String statusMessage;

  @JsonProperty("processing_cnt")
  private Integer processingCnt;

  @JsonProperty("terminated_cnt")
  private Integer terminatedCnt;

  @JsonProperty("created_at")
  private Date createdAt;

  @JsonProperty("updated_at")
  private Date updatedAt;
}
