package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt数据传输对象
 * 迁移对应关系: Thrift struct Prompt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Prompt数据传输对象")
public class PromptDTO {

  @Schema(description = "ID")
  private Long id;

  @Schema(description = "工作空间ID")
  private Long workspaceId;

  @Schema(description = "提示词键")
  private String promptKey;

  @Schema(description = "提示词基础信息")
  private PromptBasicDTO promptBasic;

  @Schema(description = "提示词草稿")
  private PromptDraftDTO promptDraft;

  @Schema(description = "提示词提交信息")
  private PromptCommitDTO promptCommit;

  @Schema(description = "目录ID")
  private Long catalogItemId;
}
