package com.iwhalecloud.bote.doc.consts;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.apache.commons.collections4.list.UnmodifiableList;

/**
 * 文档类型枚举
 */
@Getter
public enum DocumentTypeEnum {

  ROOT("ROOT", "文档库根节点"),

  /**
   * Word文档格式，上传的word文档
   */
  WORD("WORD", "Word文档"),

  /**
   * 在线Word文档（通过在线编辑器创建）
   */
  WORD_ONLINE("WORD_ONLINE", "在线文档"),

  /**
   * 在线多维表格
   */
  DIM_TABLE("DIM_TABLE", "多维表格"),

  /**
   * Excel表格格式，上传的表格文档
   */
  EXCEL("EXCEL", "Excel表格"),

  /**
   * 在线Excel表格（通过在线编辑器创建）
   */
  EXCEL_ONLINE("EXCEL_ONLINE", "在线表格"),

  /**
   * PDF格式文档，支持预览和下载
   */
  PDF("PDF", "PDF文档"),

  /**
   * PowerPoint格式，支持预览
   */
  PPT("PPT", "PPT演示文稿"),

  /**
   * 纯文本格式文档
   */
  TXT("TXT", "文本文档"),
  /**
   * 其他文件
   */
  FILE("FILE", "文件"),

  /**
   * 文本格式文档
   */
  MD("MD", "markdown文档"),

  /**
   * 图片格式文件
   */
  IMAGE("IMAGE", "图片文件"),

  /**
   * 目录结构
   */
  FOLDER("FOLDER", "文件夹");

  private final String code;
  private final String description;

  DocumentTypeEnum(String code, String description) {
    this.code = code;
    this.description = description;
  }

  /**
   * 根据代码获取枚举
   */
  public static DocumentTypeEnum getByCode(String code) {
    if (code == null) {
      return null;
    }
    for (DocumentTypeEnum type : values()) {
      if (type.getCode().equals(code)) {
        return type;
      }
    }
    return null;
  }

  public static final List<DocumentTypeEnum> ONLINE_TYPES =
    new UnmodifiableList<>(Arrays.asList(WORD_ONLINE, EXCEL_ONLINE, DIM_TABLE));

  /**
   * 判断是否为在线文档类型
   */
  public boolean isOnlineDocument() {
    return ONLINE_TYPES.contains(this);
  }

  /**
   * 判断是否为上传文档类型
   */
  public boolean isUploadDocument() {
    return !isOnlineDocument() && this != ROOT && this != FOLDER;
  }

  /**
   * 判断是否为可在线编辑的文档类型
   */
  public boolean isEditableOnline() {
    return this == WORD_ONLINE || this == EXCEL_ONLINE;
  }
}
