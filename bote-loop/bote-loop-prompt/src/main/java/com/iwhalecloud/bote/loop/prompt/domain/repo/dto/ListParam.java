package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表查询参数
 * 迁移对应关系: Go语言mysql.ListParam
 * - 功能: 通用列表查询的参数封装
 * - 字段:
 * * promptId - Prompt ID
 * * userId - 用户ID
 * * startBefore - 开始时间之前
 * * startAfter - 开始时间之后
 * * debugStep - 调试步骤
 * * limit - 限制数量
 * * debugIds - 调试ID列表
 * <p>
 * Java实现说明:
 * - 对应Go的mysql.ListParam结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go int32 -> Java Integer
 * - Go切片类型 -> Java List
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListParam {

  @JsonProperty("prompt_id")
  private Long promptId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("start_before")
  private Long startBefore;

  @JsonProperty("start_after")
  private Long startAfter;

  @JsonProperty("debug_step")
  private Integer debugStep;

  @JsonProperty("limit")
  private Integer limit;

  @JsonProperty("debug_ids")
  private List<Long> debugIds;
}
