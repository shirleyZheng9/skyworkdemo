package com.iwhalecloud.bote.dto.base;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单的流程步骤信息
 *
 * @author chen.linfa
 * @since 2025-06-30
 */
@Getter
@Setter
@ToString
public class SimpleFlowStepDTO {
  /** 节点名称 */
  private String nodeName;
  /** 节点编码 */
  private String nodeCode;
  /** 节点类型 */
  private String nodeType;
  /** 对应的流程 ID */
  private Long flowId;
  /** 执行状态 */
  private Integer stepStatus;
  /** 子节点列表 */
  private List<SimpleFlowStepDTO> children;
}
