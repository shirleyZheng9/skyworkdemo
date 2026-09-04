package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 提交草稿请求DTO
 * 对应Thrift: CommitDraftRequest
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "提交草稿请求DTO")
public class CommitDraftRequest {
  @Schema(description = "提示词ID")
  private Long promptId;
  @Schema(description = "提交版本")
  private String commitVersion;
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "提交描述")
  private String commitDescription;
  @Schema(description = "基础信息")
  private Base base;
}
