package com.iwhalecloud.bote.loop.client.prompt.debug.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.DebugContextDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取调试上下文响应DTO
 * 对应Thrift: GetDebugContextResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetDebugContextResponse {

  @Schema(description = "调试上下文")
  private DebugContextDTO debugContext;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
