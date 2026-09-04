package com.iwhalecloud.bote.loop.evaluation.domain.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 内容结构体
 * 迁移对应关系: Go语言Content
 * - 功能: 内容数据结构
 * - 字段: contentType, format, text, image, multiPart, audio
 * <p>
 * Java实现说明:
 * - 对应Go的Content结构体
 * - 使用Lombok注解简化代码
 * - 使用Jackson注解进行JSON序列化
 * - 实现GetText、SetText、GetContentType、SetContentType方法
 * <p>
 * 技术栈迁移:
 * - Go结构体 -> Java类
 * - Go *ContentType -> Java ContentType
 * - Go *string -> Java String
 * - Go []*Content -> Java List<Content>
 * - Go json标签 -> Jackson注解
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {
  private ContentType contentType;

  private FieldDisplayFormat format;

  private String text;

  private Image image;

  private List<Content> multiPart;

  private Audio audio;

  /**
   * 获取内容文本
   * 迁移对应关系: Go语言Content.GetText()
   */
  public String getText() {
    return this.text != null ? this.text : "";
  }

}
