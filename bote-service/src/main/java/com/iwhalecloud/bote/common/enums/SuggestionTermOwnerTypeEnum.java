package com.iwhalecloud.bote.common.enums;

import lombok.Getter;

/**
 * 联想术语归属者类型枚举
 *
 * @author lizuyin
 * @since 2025-01-28
 */
@Getter
public enum SuggestionTermOwnerTypeEnum {
  BOT("智能应用"),
  SCENE("智能体"),
  TENANT("项目");

  /**
   * 描述
   */
  private final String desc;

  SuggestionTermOwnerTypeEnum(String desc) {
    this.desc = desc;
  }
}
