package com.iwhalecloud.bote.llm.client.dto;

import com.iwhalecloud.bote.llm.client.consts.ToolType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.iwhalecloud.bote.llm.client.consts.ToolType.FUNCTION;

/**
 * 工具选择
 *
 * <p>用于控制模型必须调用指定的工具。</p>
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
public class ToolChoice {
  /** 工具类型 */
  private ToolType type = FUNCTION;
  /** 函数 */
  private Function function;

  public ToolChoice() {
  }

  /**
   * 根据函数构造工具选择
   */
  public ToolChoice(Function function) {
    this.function = function;
  }

  /**
   * 根据函数名称构造工具选择
   */
  public ToolChoice(String functionName) {
    this.function = new Function(functionName);
  }
}
