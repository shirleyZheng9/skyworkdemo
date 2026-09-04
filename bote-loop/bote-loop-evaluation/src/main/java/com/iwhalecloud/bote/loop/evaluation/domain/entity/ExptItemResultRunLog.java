package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 实验项结果运行日志实体
 * 迁移对应关系: Go语言ExptItemResultRunLog
 * - 功能: 实验项结果运行日志数据结构
 * - 字段: id, spaceId, exptId, exptRunId, itemId, status, errMsg, logId, resultState, updatedAt
 * <p>
 * Java实现说明:
 * - 对应Go的ExptItemResultRunLog结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go []byte -> Java byte[]
 * - Go *time.Time -> Java LocalDateTime
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExptItemResultRunLog {
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
  private Integer status;

  @JsonProperty("err_msg")
  private String errMsg;

  @JsonProperty("log_id")
  private String logId;

  @JsonProperty("result_state")
  private Integer resultState;

  @JsonProperty("updated_at")
  private Date updatedAt;
}
