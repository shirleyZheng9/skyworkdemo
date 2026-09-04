package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.BaseResp;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取Prompt响应DTO
 * 对应Thrift: BatchGetPromptResponse
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchGetPromptResponse {

  @Schema(description = "结果列表")
  private List<PromptResult> results;

  @Schema(description = "基础响应信息")
  private BaseResp baseResp;
}
