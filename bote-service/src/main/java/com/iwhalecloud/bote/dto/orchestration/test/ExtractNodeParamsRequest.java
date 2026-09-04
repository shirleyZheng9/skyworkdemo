package com.iwhalecloud.bote.dto.orchestration.test;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.scene.graph.SceneGraphDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 提取节点参数请求对象
 *
 * @author bianjp
 * @since 2025-08-04
 */
@Getter
@Setter
@ToString
@Schema(description = "提取节点参数请求对象")
public class ExtractNodeParamsRequest {
  @Schema(description = "租户 ID")
  private Long tenantId;
  @Schema(description = "节点编码")
  private String nodeCode;
  @Schema(description = "参数形式(table, json)")
  private String format;
  @Schema(description = "入参")
  private ParameterSpec request;
  @Schema(description = "变量列表")
  private List<ParameterSpec> variables;
  @Schema(description = "流程图")
  private SceneGraphDTO graph;
}
