package com.iwhalecloud.bote.loop.client.prompt.domain.prompt;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 调试核心DTO
 * 迁移对应关系: Thrift struct DebugCore
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebugCoreDTO {

  @Schema(description = "模拟上下文列表")
  private List<DebugMessageDTO> mockContexts;

  @Schema(description = "模拟变量列表")
  private List<VariableValDTO> mockVariables;

  @Schema(description = "模拟工具列表")
  private List<MockToolDTO> mockTools;
}
