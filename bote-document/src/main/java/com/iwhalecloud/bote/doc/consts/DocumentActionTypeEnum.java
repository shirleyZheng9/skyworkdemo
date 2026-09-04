package com.iwhalecloud.bote.doc.consts;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文档操作类型枚举
 *
 * @author Aiqing
 * @since 2025-01-28
 */
@Getter
@AllArgsConstructor
public enum DocumentActionTypeEnum {

  /**
   * 查看
   */
  VIEW("VIEW", "查看"),

  /**
   * 编辑
   */
  EDIT("EDIT", "编辑"),

  /**
   * 文档重命名
   */
  DOCUMENT_RENAMED("DOCUMENT_RENAMED", "重命名"),

  /**
   * 下载
   */
  DOWNLOAD("DOWNLOAD", "下载"),

  /**
   * 删除
   */
  DELETE("DELETE", "删除"),

  /**
   * 创建
   */
  CREATE("CREATE", "创建"),
  /**
   * 重新上传
   */
  RE_UPLOAD("RE_UPLOAD", "重新上传"),
  /**
   * 移出
   */
  MOVE_OUT("MOVE_OUT", "移出"),
  /**
   * 移进
   */
  MOVE_IN("MOVE_IN", "移进"),
  UPLOAD("UPLOAD", "上传");

  /**
   * 操作类型代码
   */
  private final String code;

  /**
   * 操作类型名称
   */
  private final String name;

  /**
   * 根据代码获取枚举
   *
   * @param code 操作类型代码
   * @return 枚举值
   */
  public static DocumentActionTypeEnum getByCode(String code) {
    if (code == null) {
      return null;
    }
    for (DocumentActionTypeEnum actionType : values()) {
      if (actionType.getCode().equals(code)) {
        return actionType;
      }
    }
    return null;
  }

  /**
   * 根据代码获取名称
   *
   * @param code 操作类型代码
   * @return 操作类型名称
   */
  public static String getNameByCode(String code) {
    DocumentActionTypeEnum actionType = getByCode(code);
    return actionType != null ? actionType.getName() : code;
  }
}
