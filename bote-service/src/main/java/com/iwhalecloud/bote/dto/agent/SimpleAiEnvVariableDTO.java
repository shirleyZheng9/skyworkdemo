package com.iwhalecloud.bote.dto.agent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通用智能体环境变量
 *
 * @author chen.linfa
 * @since 2026-03-10
 */
@Getter
@Setter
@ToString
public class SimpleAiEnvVariableDTO {
  /** 变量编码 */
  private String variableCode;
  /** 变量值 */
  private String variableValue;
}
