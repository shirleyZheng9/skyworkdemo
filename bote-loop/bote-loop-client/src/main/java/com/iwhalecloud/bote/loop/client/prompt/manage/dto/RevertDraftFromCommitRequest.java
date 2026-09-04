package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 从提交恢复草稿请求DTO
 * 对应Thrift: RevertDraftFromCommitRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevertDraftFromCommitRequest {
  @Schema(description = "提示词 ID")
  private Long promptId;
  @Schema(description = "源版本")
  private String commitVersionRevertingFrom;
  @Schema(description = "租户 ID")
  private Long tenantId;
  private Base base;
}
