package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt草稿信息DTO
 * 迁移对应关系: Thrift struct PromptDraft
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptDraftDTO {

  @Schema(description = "详细信息")
  private PromptDetailDTO detail;

  @Schema(description = "草稿信息")
  private DraftInfoDTO draftInfo;
}
