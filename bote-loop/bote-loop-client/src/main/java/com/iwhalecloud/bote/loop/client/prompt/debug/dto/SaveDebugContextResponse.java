package com.iwhalecloud.bote.loop.client.prompt.debug.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保存调试上下文响应DTO
 * 对应Thrift: SaveDebugContextResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveDebugContextResponse {
  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
