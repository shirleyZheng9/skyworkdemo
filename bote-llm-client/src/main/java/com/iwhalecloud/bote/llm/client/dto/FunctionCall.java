package com.iwhalecloud.bote.llm.client.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 函数调用
 *
 * @author bianjp
 * @since 2024-08-01
 */
@Getter
@Setter
@ToString
public class FunctionCall {
  /** 函数名称 */
  private String name;
  /** 参数(JSON) */
  private String arguments;

  public FunctionCall() {
  }

  public FunctionCall(String name, String arguments) {
    this.name = name;
    this.arguments = arguments;
  }
}
