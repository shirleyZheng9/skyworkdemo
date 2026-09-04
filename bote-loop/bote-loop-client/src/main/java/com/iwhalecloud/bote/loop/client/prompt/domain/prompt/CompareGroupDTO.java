package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 比较组DTO
 * 迁移对应关系: Thrift struct CompareGroup
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompareGroupDTO {

  @Schema(description = "提示词详情")
  private PromptDetailDTO promptDetail;

  @Schema(description = "调试核心信息")
  private DebugCoreDTO debugCore;
}
