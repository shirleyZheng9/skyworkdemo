package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试历史查询参数
 * 迁移对应关系: Go语言repo.ListDebugHistoryParam
 * - 功能: 调试历史查询的参数封装
 * - 字段:
 * * promptId - Prompt ID
 * * userId - 用户ID
 * * pageSize - 页面大小
 * * pageToken - 分页令牌
 * * daysLimit - 天数限制
 * <p>
 * Java实现说明:
 * - 对应Go的repo.ListDebugHistoryParam结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go int64 -> Java Long
 * - Go string -> Java String
 * - Go int32 -> Java Integer
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDebugHistoryParam {

  @JsonProperty("prompt_id")
  private Long promptId;

  @JsonProperty("user_id")
  private String userId;

  @JsonProperty("page_size")
  private Integer pageSize;

  @JsonProperty("page_token")
  private Long pageToken;

  @JsonProperty("days_limit")
  private Integer daysLimit;
}
