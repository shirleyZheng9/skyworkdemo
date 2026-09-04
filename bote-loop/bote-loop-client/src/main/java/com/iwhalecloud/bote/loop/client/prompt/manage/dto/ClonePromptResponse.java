package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 克隆Prompt响应DTO
 * 对应Thrift: ClonePromptResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClonePromptResponse {

  @Schema(description = "克隆后的提示词ID")
  private Long clonedPromptId;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
