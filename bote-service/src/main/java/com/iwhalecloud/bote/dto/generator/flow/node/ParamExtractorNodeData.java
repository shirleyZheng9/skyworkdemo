package com.iwhalecloud.bote.dto.generator.flow.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import com.iwhalecloud.bote.dto.model.MemoryConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 参数提取节点数据
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ParamExtractorNodeData {
  /** 大模型 ID */
  private Long modelId;
  /** 输入（模板字符串，支持引用变量） */
  private String input;
  /** 指令（模板字符串，支持引用变量），可选 */
  private String instruction;
  /** 要提取的参数 */
  private ParameterSpec parameters;
  /** 记忆配置，可选 */
  private MemoryConfig memory;
  /** 出参 */
  private ParameterSpec outData;
}
