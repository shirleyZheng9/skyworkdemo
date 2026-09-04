package com.iwhalecloud.bote.doc.module.document.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 审核状态枚举
 *
 * @author Aiqing
 * @since 2025-10-16
 */
@Getter
@AllArgsConstructor
public enum ReviewStatusEnum {

  /**
   * 待审核
   */
  PENDING("PENDING", "待审核"),

  /**
   * 已通过
   */
  APPROVED("APPROVED", "已通过"),

  /**
   * 已拒绝
   */
  REJECTED("REJECTED", "已拒绝");

  private final String code;
  private final String desc;
}
