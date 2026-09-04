package com.iwhalecloud.bote.doc.module.document.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * ydoc 文档的内容格式
 *
 * @author Aiqing
 * @since 2025/8/31
 */
@Getter
@Setter
@ToString
public class YdocContent {

  /**
   * 文档类型，默认doc
   */
  private String type;
  /**
   * 文档的内容，json结构
   */
  private List<Object> content;

  public static YdocContent emptyContent() {
    YdocContent content = new YdocContent();
    content.setType("doc");
    content.setContent(new ArrayList<>());
    return content;
  }
}
