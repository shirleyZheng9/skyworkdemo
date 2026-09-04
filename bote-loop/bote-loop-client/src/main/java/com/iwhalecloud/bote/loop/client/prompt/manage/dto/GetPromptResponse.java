package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDetailDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取Prompt响应DTO
 * 对应Thrift: GetPromptResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "获取Prompt响应DTO")
public class GetPromptResponse {

  @Schema(description = "提示词")
  private PromptDTO prompt;

  @Schema(description = "默认配置")
  private PromptDetailDTO defaultConfig;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
