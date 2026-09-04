package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试配置DTO
 * 迁移对应关系: Thrift struct DebugConfig
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugConfigDTO {

  @Schema(description = "是否单步调试")
  private Boolean singleStepDebug;
}
