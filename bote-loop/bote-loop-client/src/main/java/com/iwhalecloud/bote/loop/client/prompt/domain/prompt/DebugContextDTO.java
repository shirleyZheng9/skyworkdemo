package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试上下文DTO
 * 迁移对应关系: Thrift struct DebugContext
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugContextDTO {

  @Schema(description = "调试核心信息")
  private DebugCoreDTO debugCore;

  @Schema(description = "调试配置信息")
  private DebugConfigDTO debugConfig;

  @Schema(description = "比较配置信息")
  private CompareConfigDTO compareConfig;
}
