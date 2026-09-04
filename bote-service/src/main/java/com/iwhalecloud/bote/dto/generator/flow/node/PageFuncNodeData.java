package com.iwhalecloud.bote.dto.generator.flow.node;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 页面函数节点数据
 *
 * @author bianjp
 * @since 2025-03-31
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class PageFuncNodeData {
  /** 页面函数 ID */
  private Long pageFuncId;
  /** 参数 */
  private ParameterSpec parameters;
}
