package com.iwhalecloud.bote.llm.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.llm.client.dto.schema.JsonSchemaNode;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 函数
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class Function {
  /** 空参数 */
  public static final JsonSchemaNode EMPTY_PARAMETER = JsonSchemaNode.newObject(null, Map.of(), null);

  /** 函数名称(只能包含字母、数字、下划线、连字符，长度不超过 64 个字符) */
  private String name;
  /** 函数描述 */
  private String description;
  /** 参数规格(JSON Schema 格式) */
  private JsonSchemaNode parameters;

  public Function() {
  }

  public Function(String name) {
    this(name, null, null);
  }

  public Function(String name, String description) {
    this(name, description, null);
  }

  public Function(String name, String description, JsonSchemaNode parameters) {
    this.name = name;
    this.description = description;
    // 确保参数不为空，否则某些大模型会报错，比如 MiniMax-M2.5
    this.parameters = parameters == null ? EMPTY_PARAMETER : parameters;
  }
}
