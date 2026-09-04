package com.iwhalecloud.bote.loop.client.prompt.debug.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 列表调试历史请求DTO
 * 对应Thrift: ListDebugHistoryRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListDebugHistoryRequest {

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "天数限制")
  private Integer daysLimit;

  @Schema(description = "页面大小")
  private Integer pageSize;

  @Schema(description = "页面令牌")
  private String pageToken;

  @Schema(description = "基础信息")
  private Base base;
}
