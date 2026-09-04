package com.iwhalecloud.bote.doc.consts;

import java.util.Arrays;
import lombok.Getter;

/**
 * 文档的权限模式
 *
 * @author Aiqing
 * @since 2025/10/14
 */
@Getter
public enum PermissionModeEnum {

  /**
   * 继承模式
   */
  INHERIT(0, "继承模式"),

  /**
   * 独立模式
   */
  ASSIGN(1, "独立模式");

  private final int code;

  private final String name;

  PermissionModeEnum(int code, String name) {
    this.code = code;
    this.name = name;
  }

  public static PermissionModeEnum getByCode(int code) {
    return Arrays.stream(values())
      .filter(item -> item.getCode() == code)
      .findFirst()
      .orElse(null);
  }
}
