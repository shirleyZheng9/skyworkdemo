package com.iwhalecloud.bote.llm.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.iwhalecloud.bote.llm.client.consts.ToolType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 工具调用
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
@JsonInclude(Include.NON_NULL)
public class ToolCall {
  /** 编号，从 0 开始 */
  private Integer index;
  /** 工具调用标识 */
  private String id;
  /** 工具类型 */
  private ToolType type;
  /** 函数 */
  private FunctionCall function;

  public ToolCall() {
    this.type = ToolType.FUNCTION;
  }

  public ToolCall(Integer index, String id, String toolName, String toolArguments) {
    this.index = index;
    this.id = id;
    this.type = ToolType.FUNCTION;
    this.function = new FunctionCall(toolName, toolArguments);
  }

  public ToolCall(String id, String toolName, String toolArguments) {
    this.id = id;
    this.type = ToolType.FUNCTION;
    this.function = new FunctionCall(toolName, toolArguments);
  }

  public ToolCall(String id, FunctionCall function) {
    this.id = id;
    this.type = ToolType.FUNCTION;
    this.function = function;
  }
}
