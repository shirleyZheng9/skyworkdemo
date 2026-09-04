package com.iwhalecloud.bote.loop.client.prompt.debug.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugContextDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保存调试上下文请求DTO
 * 对应Thrift: SaveDebugContextRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveDebugContextRequest {

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "调试上下文")
  private DebugContextDTO debugContext;

  @Schema(description = "基础信息")
  private Base base;
}
