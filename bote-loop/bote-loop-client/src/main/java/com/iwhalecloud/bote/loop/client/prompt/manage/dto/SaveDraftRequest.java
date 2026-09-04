package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDraftDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保存草稿请求DTO
 * 对应Thrift: SaveDraftRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "保存草稿请求DTO")
public class SaveDraftRequest {
  @Schema(description = "提示词ID")
  private Long promptId;
  @Schema(description = "提示词草稿")
  private PromptDraftDTO promptDraft;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "基础信息")
  private Base base;
}
