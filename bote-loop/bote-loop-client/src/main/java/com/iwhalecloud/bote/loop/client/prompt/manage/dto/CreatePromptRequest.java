package com.iwhalecloud.bote.loop.client.prompt.manage.dto;

import com.iwhalecloud.bote.loop.client.base.Base;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptDetailDTO;
import com.iwhalecloud.bote.loop.client.prompt.domain.prompt.PromptType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 创建Prompt请求DTO
 * 对应Thrift: CreatePromptRequest
 */
@Getter
@Setter
@ToString
@Schema(description = "创建Prompt请求DTO")
public class CreatePromptRequest {

  @Schema(description = "工作空间ID")
  private Long workspaceId;
  @Schema(description = "Prompt名称")
  private String promptName;
  @Schema(description = "Prompt键")
  private String promptKey;
  @Schema(description = "Prompt描述")
  private String promptDescription;
  @Schema(description = "草稿详情")
  private PromptDetailDTO draftDetail;
  @Schema(description = "基础信息")
  private Base base;
  @Schema(description = "目录ID")
  private Long catalogItemId;
  @Schema(description = "Prompt类型", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull(message = "Prompt类型不能为空")
  private PromptType promptType;
}
