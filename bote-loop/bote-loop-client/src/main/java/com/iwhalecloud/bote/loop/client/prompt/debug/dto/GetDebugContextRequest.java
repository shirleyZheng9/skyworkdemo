package com.iwhalecloud.bote.loop.client.prompt.debug.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取调试上下文请求DTO
 * 对应Thrift: GetDebugContextRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDebugContextRequest {

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "基础信息")
  private Base base;
}
