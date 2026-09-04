package com.iwhalecloud.bote.doc.consts;

import lombok.Getter;

/**
 * 各实体记录ID前缀枚举
 *
 * @author Aiqing
 * @since 2025/8/14
 */
@Getter
public enum IdRulePrefixEnum {

  /**
   * root 节点
   */
  ROOT("rot"),

  /**
   * 文档库
   */
  LIBRARY("lbr"),

  /**
   * 知识库
   */
  KNOWLEDGE_BASE("klb"),

  /**
   * 文档
   */
  DOCUMENT("doc"),

  /**
   * 表格
   */
  WORKBOOK("wkb"),

  /**
   * 表格sheet
   */
  SHEET("sht"),

  /**
   * 多维表格
   */
  DIM_TABLE("dtb"),

  /**
   * 文件夹
   */
  FOLD("fod"),
  /**
   * 文件
   */
  NODE("nod");


  private final String prefix;

  IdRulePrefixEnum(String prefix) {
    this.prefix = prefix;
  }
}
