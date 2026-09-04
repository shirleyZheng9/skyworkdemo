package com.iwhalecloud.bote.dto.orchestration.test;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 支持单节点调试的节点类型信息
 *
 * @author bianjp
 * @since 2025-08-05
 */
@Getter
@Setter
@ToString
@Schema(description = "支持单节点调试的节点类型信息")
public class DebuggableNodeTypesDTO {
  @Schema(description = "支持单节点调试的节点类型列表")
  private List<String> nodeTypes;
  @Schema(description = "需要使用流式输出的节点类型列表")
  private List<String> streamNodeTypes;
}
