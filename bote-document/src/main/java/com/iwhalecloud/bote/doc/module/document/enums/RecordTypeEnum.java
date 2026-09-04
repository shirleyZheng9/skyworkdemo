package com.iwhalecloud.bote.doc.module.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 记录类型枚举
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@AllArgsConstructor
public enum RecordTypeEnum {

  /**
   * 修订记录
   */
  CORRECTION("CORRECTION", "修订记录"),

  /**
   * 评论记录
   */
  COMMENT("COMMENT", "评论记录");

  private final String code;
  private final String desc;
}
