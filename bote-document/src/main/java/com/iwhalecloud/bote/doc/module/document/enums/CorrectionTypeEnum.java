package com.iwhalecloud.bote.doc.module.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 修订类型枚举
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@AllArgsConstructor
public enum CorrectionTypeEnum {

  /**
   * 修正
   */
  MODIFY("MODIFY", "修正"),

  /**
   * 新增
   */
  ADD("ADD", "新增"),

  /**
   * 删除
   */
  DELETE("DELETE", "删除"),

  /**
   * 设置
   */
  SET("SET", "设置");

  private final String code;
  private final String desc;
}
