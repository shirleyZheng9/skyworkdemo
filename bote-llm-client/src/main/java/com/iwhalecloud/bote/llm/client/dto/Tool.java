package com.iwhalecloud.bote.llm.client.dto;

import com.iwhalecloud.bote.llm.client.consts.ToolType;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工具
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
public class Tool {
  /** 工具类型 */
  private ToolType type = ToolType.FUNCTION;
  /** 函数 */
  private Function function;

  public Tool() {
  }

  public Tool(Function function) {
    this.function = function;
  }

  public Tool(String name, String description, JsonSchemaNode parameters) {
    this(new Function(name, description, parameters));
  }
}
