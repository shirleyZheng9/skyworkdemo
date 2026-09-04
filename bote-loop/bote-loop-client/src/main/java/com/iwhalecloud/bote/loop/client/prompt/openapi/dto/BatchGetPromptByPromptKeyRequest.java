package com.iwhalecloud.bote.loop.client.prompt.openapi.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量获取Prompt请求DTO
 * 对应Thrift: BatchGetPromptByPromptKeyRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "批量获取Prompt请求DTO")
public class BatchGetPromptByPromptKeyRequest {

  /**
   * 工作空间ID
   * 对应Thrift字段: workspace_id
   */
  @Schema(description = "工作空间ID")
  private Long workspaceId;

  /**
   * 查询列表
   * 对应Thrift字段: queries
   */
  @Schema(description = "查询列表")
  private List<PromptQuery> queries;

  /**
   * 基础请求信息
   * 对应Thrift字段: Base
   */
  @Schema(description = "基础请求信息")
  private Base base;
}
