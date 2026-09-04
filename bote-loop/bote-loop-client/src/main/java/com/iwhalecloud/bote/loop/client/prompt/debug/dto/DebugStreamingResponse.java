package com.iwhalecloud.bote.loop.client.prompt.debug.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.TokenUsageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试流式响应DTO
 * 对应Thrift: DebugStreamingResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugStreamingResponse {

  @Schema(description = "增量消息")
  private MessageDTO delta;

  @Schema(description = "结束原因")
  private String finishReason;

  @Schema(description = "令牌用量")
  private TokenUsageDTO usage;

  @Schema(description = "调试ID")
  private Long debugId;

  @Schema(description = "调试追踪Key")
  private String debugTraceKey;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
