package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除Prompt响应DTO
 * 对应Thrift: DeletePromptResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletePromptResponse {

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
