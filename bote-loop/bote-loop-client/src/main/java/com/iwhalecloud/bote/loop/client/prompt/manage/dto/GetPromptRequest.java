package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 获取Prompt请求DTO
 * 对应Thrift: GetPromptRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "获取Prompt请求DTO")
public class GetPromptRequest {

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "是否包含提交信息")
  private Boolean withCommit;

  @Schema(description = "提交版本")
  private String commitVersion;

  @Schema(description = "是否包含草稿")
  private Boolean withDraft;

  @Schema(description = "是否包含默认配置")
  private Boolean withDefaultConfig;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "基础信息")
  private Base base;
}
