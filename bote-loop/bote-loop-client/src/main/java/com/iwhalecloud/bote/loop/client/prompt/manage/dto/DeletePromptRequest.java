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
 * 删除Prompt请求DTO
 * 对应Thrift: DeletePromptRequest
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletePromptRequest {
  @Schema(description = "提示词ID")
  private Long promptId;
  @Schema(description = "租户ID")
  protected Long tenantId;
  private Base base;
}
