package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建Prompt响应DTO
 * 对应Thrift: CreatePromptResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "创建Prompt响应DTO")
public class CreatePromptResponse {

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
