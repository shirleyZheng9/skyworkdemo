package com.iwhalecloud.bote.dto.generator.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简化的流程线条，用于与大模型交互
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SimplifiedEdgeDTO {
  /** 起始节点 */
  private String from;
  /** 目标节点 */
  private String to;
  /** 标签，同一节点连接多个目标节点时用于区分不同线条的含义 */
  private String label;
}
