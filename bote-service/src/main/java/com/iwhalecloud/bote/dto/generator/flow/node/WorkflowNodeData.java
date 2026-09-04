package com.iwhalecloud.bote.dto.generator.flow.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 工作流节点数据
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WorkflowNodeData {
  /** 流程 ID */
  private Long flowId;
  /** 参数 */
  private ParameterSpec parameters;
  /** 出参 */
  private ParameterSpec outData;
}
