package com.iwhalecloud.bote.doc.consts;

/**
 * 设置操作类型枚举
 */
public enum SettingsActionEnum {

  /**
   * 更新文档库名称、描述、图标等
   */
  UPDATE_INFO("UPDATE_INFO", "更新基础信息"),

  /**
   * 修改文档库的可见范围设置
   */
  UPDATE_VISIBILITY("UPDATE_VISIBILITY", "更新可见范围");

  private final String code;
  private final String description;

  SettingsActionEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 根据代码获取枚举
   */
  public static SettingsActionEnum fromCode(String code) {
    for (SettingsActionEnum action : values()) {
      if (action.getCode().equals(code)) {
        return action;
      }
    }
    throw new IllegalArgumentException("未知的操作类型: " + code);
  }

  public String getCode() {
    return code;
  }

  public String getDescription() {
    return description;
  }
}
