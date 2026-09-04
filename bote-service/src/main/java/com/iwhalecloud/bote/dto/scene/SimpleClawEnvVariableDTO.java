package com.iwhalecloud.bote.dto.scene;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 简单 claw 环境变量
 *
 * @author chen.linfa
 * @since 2026-04-23
 */
@Getter
@Setter
@ToString
public class SimpleClawEnvVariableDTO {
  /** 主键 */
  private Long id;
  /** 环境编码 */
  private String variableCode;
  /** 环境编码值 */
  private String variableVal;
}
