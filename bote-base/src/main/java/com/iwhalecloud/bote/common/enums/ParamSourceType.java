package com.iwhalecloud.bote.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 参数来源类型
 *
 * @author bianjp
 * @since 2024-08-29
 */
@Getter
@RequiredArgsConstructor
public enum ParamSourceType {
  /** 入参 */
  INPUT("$.input"),
  /** 步骤输出 */
  STEP_OUTPUT("$.step"),
  /** 循环变量 */
  LOOP_VARIABLE("$.loop"),
  /** 全局变量 */
  GLOBAL_VARIABLE("$.variable"),
  /** 系统变量 */
  SYSTEM_VARIABLE("$.system"),
  /** 上下文变量 */
  CONTEXT_VARIABLE("$.context"),
  /** 登录信息 */
  SESSION("$.session"),
  /** Cookie信息 */
  COOKIE("$.cookie"),
  /** 项目环境变量 */
  ENV_VARIABLE("$.envVar"),
  /** 字面量 */
  LITERAL("");

  /** 前缀 */
  private final String prefix;

  /**
   * 从属性描述字符串中删除前缀
   */
  public String removePrefix(String spec) {
    // 删除前缀和紧跟前缀的 "."
    // 有些情况可能只有前缀，比如引用整个循环变量: $.loop
    return spec.length() > (prefix.length() + 1) ? spec.substring(prefix.length() + 1) : "";
  }
}
