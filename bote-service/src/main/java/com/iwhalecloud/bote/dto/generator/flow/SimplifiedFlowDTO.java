package com.iwhalecloud.bote.dto.generator.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简化的流程对象，用于与大模型交互
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SimplifiedFlowDTO {
  /** 流程名称 */
  private String name;
  /** 流程编码 */
  private String code;
  @JsonIgnore
  private String flowType;
  /** 入参 */
  private ParameterSpec request;
  /** 出参 */
  private ParameterSpec response;
  /** 变量列表 */
  private List<ParameterSpec> variables;
  /** 节点列表 */
  private List<SimplifiedNodeDTO> nodes;
  /** 线条列表 */
  private List<SimplifiedEdgeDTO> edges;
}
