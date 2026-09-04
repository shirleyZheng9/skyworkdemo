package com.iwhalecloud.bote.loop.client.prompt.debug.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MockToolDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.VariableValDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试流式请求DTO
 * 对应Thrift: DebugStreamingRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugStreamingRequest {

  @Schema(description = "提示词信息")
  private PromptDTO prompt;

  @Schema(description = "消息列表")
  private List<MessageDTO> messages;

  @Schema(description = "变量值列表")
  private List<VariableValDTO> variableVals;

  @Schema(description = "模拟工具列表")
  private List<MockToolDTO> mockTools;

  @Schema(description = "是否单步调试")
  private Boolean singleStepDebug;

  @Schema(description = "调试跟踪键")
  private String debugTraceKey;

  @Schema(description = "基础信息")
  private Base base;
  @Schema(description = "客户端 ID(流式接口中断请求使用)")
  private String clientId;
}
