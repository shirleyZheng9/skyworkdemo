package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取Prompt请求DTO
 * 对应Thrift: BatchGetPromptRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取Prompt请求DTO")
public class BatchGetPromptRequest {

  @Schema(description = "查询列表")
  private List<PromptQuery> queries;

  @Schema(description = "基础信息")
  private Base base;
}
