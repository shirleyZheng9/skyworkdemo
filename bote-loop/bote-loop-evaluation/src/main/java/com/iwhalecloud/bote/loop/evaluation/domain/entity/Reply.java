package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回复实体
 * 迁移对应关系: Go语言Reply
 * - 功能: 回复数据结构
 * - 字段: item, debugId, debugStep, debugTraceKey
 * <p>
 * Java实现说明:
 * - 对应Go的Reply结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *ReplyItem -> Java ReplyItem
 * - Go int64 -> Java Long
 * - Go int32 -> Java Integer
 * - Go string -> Java String
 * - Go json标签 -> Jackson注解
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply {
  @JsonProperty("item")
  private ReplyItem item;

  @JsonProperty("debug_id")
  private Long debugId;

  @JsonProperty("debug_step")
  private Integer debugStep;

  @JsonProperty("debug_trace_key")
  private String debugTraceKey;
}
