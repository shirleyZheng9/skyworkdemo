package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新Prompt请求DTO
 * 对应Thrift: UpdatePromptRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "更新Prompt请求DTO")
public class UpdatePromptRequest {

  @Schema(description = "提示词ID")
  private Long promptId;

  @Schema(description = "提示词名称")
  private String promptName;
  @Schema(description = "租户ID")
  private Long tenantId;
  @Schema(description = "提示词描述")
  private String promptDescription;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "Prompt类型")
  private PromptType promptType;
  @Schema(description = "基础信息")
  private Base base;
}
