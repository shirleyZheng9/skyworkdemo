package com.iwhalecloud.bote.common.enums;

import lombok.Getter;

/**
 * 联想话术类型枚举
 *
 * @author lizuyin
 * @since 2025-06-09
 */
@Getter
public enum SuggestionTermTypeEnum {
  WORD("热词"),
  TERM("术语");

  /**
   * 描述
   */
  private final String desc;

  SuggestionTermTypeEnum(String desc) {
    this.desc = desc;
  }

}
