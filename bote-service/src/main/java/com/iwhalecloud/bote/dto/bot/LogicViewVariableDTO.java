package com.iwhalecloud.bote.dto.bot;

import com.iwhalecloud.bote.dto.base.ParameterSpec;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 逻辑视图变量 DTO
 *
 * @author chen.linfa
 * @since 2024-09-05
 */
@Getter
@Setter
@ToString
public class LogicViewVariableDTO {
  /** 名称 */
  private String name;
  /** 描述 */
  private String description;
  /** 取值 */
  private String value;
  /** 默认值 */
  private String defaultValue;

  public LogicViewVariableDTO(ParameterSpec parameter) {
    this.name = parameter.getName();
    this.description = parameter.getDescription();
    this.value = parameter.getValue();
    this.defaultValue = parameter.getDefaultValue();
  }
}
