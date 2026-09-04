package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 克隆Prompt请求DTO
 * 对应Thrift: ClonePromptRequest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "克隆Prompt请求DTO")
public class ClonePromptRequest {

  @Schema(description = "提示词ID")
  private Long promptId;
  @Schema(description = "工作空间ID")
  private Long workspaceId;
  @Schema(description = "提交版本")
  private String commitVersion;

  @Schema(description = "克隆的提示词名称")
  private String clonedPromptName;

  @Schema(description = "克隆的提示词键")
  private String clonedPromptKey;

  @Schema(description = "克隆的提示词描述")
  private String clonedPromptDescription;
  @Schema(description = "目录 ID")
  private Long catalogItemId;
  @Schema(description = "Prompt类型")
  private PromptType promptType;
  @Schema(description = "基础信息")
  private Base base;
}
