package com.iwhalecloud.bote.loop.client.prompt.execute.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.MessageDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.TokenUsageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部执行响应DTO
 * 对应Thrift: ExecuteInternalResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "内部执行响应DTO")
public class ExecuteInternalResponse {

  @Schema(description = "消息")
  private MessageDTO message;

  @Schema(description = "完成原因")
  private String finishReason;

  @Schema(description = "使用情况")
  private TokenUsageDTO usage;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
