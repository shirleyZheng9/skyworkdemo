package com.iwhalecloud.bote.common.enums;

import lombok.Getter;

/**
 * es文档功能类别枚举
 *
 * @author lizuyin
 * @since 2025-06-20
 */
@Getter
public enum BoteEsDocumentTypeEnum {
  SUGGESTION_TERM("联想术语"),
  SUGGESTION_WORD("联想热词"),
  COMMON("通用");

  /** 描述 */
  private final String desc;

  BoteEsDocumentTypeEnum(String desc) {
    this.desc = desc;
  }

  /**
   * 获取匹配的枚举名称（如果存在）。
   *
   * @param type 要匹配的类型名称
   * @return 匹配成功的枚举名称；否则返回 null
   */
  public static String getMatchingType(String type) {
    for (BoteEsDocumentTypeEnum value : values()) {
      if (value.name().equals(type)) {
        return value.name();
      }
    }
    return null;
  }

}
