package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt提交信息DTO
 * 迁移对应关系: Thrift struct PromptCommit
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptCommitDTO {

  @Schema(description = "详细信息")
  private PromptDetailDTO detail;

  @Schema(description = "提交信息")
  private CommitInfoDTO commitInfo;
}
