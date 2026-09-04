package com.iwhalecloud.bote.loop.prompt.domain.repo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.iwhalecloud.bote.loop.prompt.domain.entity.DebugLog;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试历史查询结果
 * 迁移对应关系: Go语言repo.ListDebugHistoryResult
 * - 功能: 调试历史查询的结果封装
 * - 字段:
 * * debugHistory - 调试历史列表
 * * nextPageToken - 下一页令牌
 * * hasMore - 是否有更多
 * <p>
 * Java实现说明:
 * - 对应Go的repo.ListDebugHistoryResult结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go切片类型 -> Java List
 * - Go int64 -> Java Long
 * - Go bool -> Java Boolean
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDebugHistoryResult {

  @JsonProperty("debug_history")
  private List<DebugLog> debugHistory;

  @JsonProperty("next_page_token")
  private Long nextPageToken;

  @JsonProperty("has_more")
  private Boolean hasMore;
}
