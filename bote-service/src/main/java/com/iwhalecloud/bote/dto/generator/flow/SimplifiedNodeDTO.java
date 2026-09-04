package com.iwhalecloud.bote.dto.generator.flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简化的流程节点，用于与大模型交互
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class SimplifiedNodeDTO {
  /** 节点名称 */
  private String name;
  /** 节点编码 */
  private String code;
  /** 节点类型 */
  private String type;
  /** 节点配置数据 */
  private Map<String, Object> data;
}
