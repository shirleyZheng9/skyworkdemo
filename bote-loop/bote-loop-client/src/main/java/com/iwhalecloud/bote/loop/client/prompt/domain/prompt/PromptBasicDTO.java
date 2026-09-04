package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt基础信息DTO
 * 迁移对应关系: Thrift struct PromptBasic
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptBasicDTO {

  @Schema(description = "显示名称")
  private String displayName;

  @Schema(description = "描述")
  private String description;

  @Schema(description = "最新版本")
  private String latestVersion;

  @Schema(description = "创建者")
  private String createdBy;

  @Schema(description = "更新者")
  private String updatedBy;

  @Schema(description = "创建时间")
  private Long createdAt;

  @Schema(description = "更新时间")
  private Long updatedAt;

  @Schema(description = "最新提交时间")
  private Long latestCommittedAt;

  @Schema(description = "Prompt类型")
  private PromptType promptType;
}
