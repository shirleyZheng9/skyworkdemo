package com.iwhalecloud.bote.dto.plugin.response;

import com.iwhalecloud.bote.common.enums.ToolOutputType;
import com.iwhalecloud.bote.dto.base.ParameterSpec;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工具出参配置
 *
 * @author bianjp
 * @since 2025-11-17
 */
@Getter
@Setter
@ToString
public class ToolOutputSpec {
  /** 类型 */
  private ToolOutputType type;
  /** 参数列表 */
  private List<ParameterSpec> parameters;
}
